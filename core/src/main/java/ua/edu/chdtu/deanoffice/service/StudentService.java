package ua.edu.chdtu.deanoffice.service;

import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.Student;
import ua.edu.chdtu.deanoffice.repository.StudentRepository;

import static ua.edu.chdtu.deanoffice.util.PersonUtil.toCapitalizedCase;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> findAllByIds(Integer[] id) {
        return studentRepository.getAllByStudentIds(id);
    }

    public List<Student> searchByFullName(String name, String surname, String patronimic) {
        return studentRepository.findAllByFullNameUkr(
                toCapitalizedCase(name),
                toCapitalizedCase(surname),
                toCapitalizedCase(patronimic)
        );
    }

    public Student getById(Integer studentId) {
        return this.studentRepository.findOne(studentId);
    }

    public Student save(Student student) {
        student.setName(toCapitalizedCase(student.getName()));
        student.setSurname(toCapitalizedCase(student.getSurname()));
        student.setPatronimic(toCapitalizedCase(student.getPatronimic()));
        student.setNameEng(toCapitalizedCase(student.getNameEng()));
        student.setSurnameEng(toCapitalizedCase(student.getSurnameEng()));
        student.setPatronimicEng(toCapitalizedCase(student.getPatronimicEng()));
        return this.studentRepository.save(student);
    }
}
