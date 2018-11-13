package ua.edu.chdtu.deanoffice.api.student;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.api.general.ExceptionToHttpCodeMapUtil;
import ua.edu.chdtu.deanoffice.api.general.mapper.Mapper;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentDegreeFullNameDTO;
import ua.edu.chdtu.deanoffice.api.student.dto.PreviousDiplomaDTO;
import ua.edu.chdtu.deanoffice.api.student.dto.StudentDTO;
import ua.edu.chdtu.deanoffice.api.student.dto.StudentDegreeDTO;
import ua.edu.chdtu.deanoffice.api.student.dto.StudentView;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.entity.EducationDocument;
import ua.edu.chdtu.deanoffice.entity.Student;
import ua.edu.chdtu.deanoffice.entity.StudentDegree;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.exception.OperationCannotBePerformedException;
import ua.edu.chdtu.deanoffice.service.StudentDegreeService;
import ua.edu.chdtu.deanoffice.service.StudentGroupService;
import ua.edu.chdtu.deanoffice.service.StudentService;
import ua.edu.chdtu.deanoffice.webstarter.security.CurrentUser;

import java.net.URI;
import java.util.List;

import static ua.edu.chdtu.deanoffice.api.general.Util.getNewResourceLocation;

@RestController
public class StudentDegreeController {
    private final StudentDegreeService studentDegreeService;
    private final StudentService studentService;
    private final StudentGroupService studentGroupService;

    @Autowired
    public StudentDegreeController(
            StudentDegreeService studentDegreeService,
            StudentService studentService,
            StudentGroupService studentGroupService
    ) {
        this.studentDegreeService = studentDegreeService;
        this.studentService = studentService;
        this.studentGroupService = studentGroupService;
    }

    @JsonView(StudentView.Simple.class)
    @GetMapping("/students/degrees")
    public ResponseEntity getActiveStudentsDegree(@CurrentUser ApplicationUser user) {
        try {
            return ResponseEntity.ok(getActiveStudentDegrees(user.getFaculty().getId()));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @JsonView(StudentView.Detail.class)
    @GetMapping("/students/degrees/more-detail")
    public ResponseEntity getActiveStudentsDegree_moreDetail(@CurrentUser ApplicationUser user) {
        try {
            return ResponseEntity.ok(getActiveStudentDegrees(user.getFaculty().getId()));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private List<StudentDegreeDTO> getActiveStudentDegrees(int facultyId) {
        return Mapper.map(studentDegreeService.getAllByActive(true, facultyId), StudentDegreeDTO.class);
    }

    @JsonView(StudentView.Degree.class)
    @PostMapping("/students/degrees")
    public ResponseEntity createNewStudentDegree(
            @RequestBody StudentDegreeDTO newStudentDegree,
            @RequestParam(value = "new_student", defaultValue = "false", required = false) boolean newStudent
    ) {
        try {
            Student student;
            if (newStudent) {
                student = createStudent(newStudentDegree.getStudent());
            } else {
                student = studentService.findById(newStudentDegree.getStudent().getId());
            }
            StudentDegree studentDegree = createStudentDegree(newStudentDegree, student);

            URI location = getNewResourceLocation(studentDegree.getId());
            return ResponseEntity.created(location).body(Mapper.map(studentDegree, StudentDegreeDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private Student createStudent(StudentDTO newStudentDTO) {
        Student newStudent = (Student) Mapper.strictMap(newStudentDTO, Student.class);
        if (newStudent.getId() != 0) {
            newStudent.setId(0);
        }
        return studentService.save(newStudent);
    }

    private StudentDegree createStudentDegree(StudentDegreeDTO newStudentDegreeDTO, Student student) {
        StudentDegree newStudentDegree = (StudentDegree) Mapper.strictMap(newStudentDegreeDTO, StudentDegree.class);
        newStudentDegree.setStudent(student);
        newStudentDegree.setStudentGroup(studentGroupService.getById(newStudentDegreeDTO.getStudentGroupId()));
        newStudentDegree.setSpecialization(newStudentDegree.getStudentGroup().getSpecialization());
        newStudentDegree.setActive(true);

        PreviousDiplomaDTO previousDiplomaDTO = getPreviousDiploma(newStudentDegree);
        newStudentDegree.setPreviousDiplomaType(previousDiplomaDTO.getType());
        boolean degreeIsNotBachelor = newStudentDegree.getSpecialization().getDegree().getId() != 1;
        if (degreeIsNotBachelor) {
            newStudentDegree.setPreviousDiplomaNumber(previousDiplomaDTO.getNumber());
            newStudentDegree.setPreviousDiplomaDate(previousDiplomaDTO.getDate());
        }
        return studentDegreeService.save(newStudentDegree);
    }

    private PreviousDiplomaDTO getPreviousDiploma(StudentDegree studentDegree) {
        EducationDocument educationDocument = getPreviousDiplomaType(studentDegree);
        StudentDegree firstStudentDegree = studentDegreeService.getFirst(studentDegree.getStudent().getId());
        if (firstStudentDegree != null) {
            return new PreviousDiplomaDTO(
                    firstStudentDegree.getPreviousDiplomaDate(),
                    firstStudentDegree.getPreviousDiplomaNumber(),
                    educationDocument
            );
        }
        return new PreviousDiplomaDTO(educationDocument);
    }

    private EducationDocument getPreviousDiplomaType(StudentDegree studentDegree) {
        if (EducationDocument.isExist(studentDegree.getPreviousDiplomaType())) {
            return studentDegree.getPreviousDiplomaType();
        }
        return EducationDocument.getForecastedDiplomaTypeByDegree(studentDegree.getSpecialization().getDegree().getId());
    }

    @JsonView(StudentView.Degrees.class)
    @GetMapping("/students/{id}/degrees")
    public ResponseEntity getAllStudentsDegreeById(@PathVariable("id") Integer studentId) {
        try {
            Student student = studentService.findById(studentId);
            return ResponseEntity.ok(Mapper.map(student, StudentDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @JsonView(StudentView.Degrees.class)
    @PutMapping("/students/{id}/degrees")
    public ResponseEntity updateStudentDegrees(
            @PathVariable("id") Integer studentId,
            @RequestBody List<StudentDegreeDTO> studentDegreesDTOs,
            @CurrentUser ApplicationUser user
    ) {
        try {
            validateStudentDegreesUpdates(studentDegreesDTOs);
            List<StudentDegree> studentDegrees = Mapper.strictMap(studentDegreesDTOs, StudentDegree.class);
            studentDegreeService.verifyAccess(user, studentDegrees);
            Student student = studentService.findById(studentId);

            studentDegrees.forEach(studentDegree -> {
                Integer groupId = studentDegreesDTOs.get(studentDegrees.indexOf(studentDegree)).getStudentGroupId();
                studentDegree.setStudentGroup(getStudentGroup(groupId));
                studentDegree.setSpecialization(studentDegree.getStudentGroup().getSpecialization());
                studentDegree.setStudent(student);
                studentDegree.getStudentPreviousUniversities().forEach(studentPreviousUniversity -> studentPreviousUniversity.setStudentDegree(studentDegree));
            });

            studentDegreeService.update(studentDegrees);
            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private void validateStudentDegreesUpdates(List<StudentDegreeDTO> studentDegreesDTOs) throws OperationCannotBePerformedException {
        if (isAnyStudentDegreeMissingId(studentDegreesDTOs)) {
            String exceptionMessage = "Отримано некоректну інформацію. Зверністься до адміністратора або розробника системи";
            throw new OperationCannotBePerformedException(exceptionMessage);
        }
    }


    private boolean isAnyStudentDegreeMissingId(List<StudentDegreeDTO> studentDegreeDTOs) {
        return studentDegreeDTOs.stream().anyMatch((studentDegreeDTO) -> studentDegreeDTO.getId() == null);
    }

    private StudentGroup getStudentGroup(Integer groupId) {
        return this.studentGroupService.getById(groupId);
    }

    @GetMapping("/groups/{group_id}/students")
    public ResponseEntity getStudentsByGroupId(@PathVariable("group_id") Integer groupId) {
        try {
            List<StudentDegree> students = this.studentDegreeService.getAllByGroupId(groupId);
            return ResponseEntity.ok(Mapper.map(students, StudentDegreeFullNameDTO.class));
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private ResponseEntity handleException(Exception exception) {
        return ExceptionHandlerAdvice.handleException(exception, StudentDegreeController.class, ExceptionToHttpCodeMapUtil.map(exception));
    }
}
