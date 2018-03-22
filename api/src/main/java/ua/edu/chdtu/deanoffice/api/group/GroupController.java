package ua.edu.chdtu.deanoffice.api.group;

import com.fasterxml.jackson.annotation.JsonView;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.chdtu.deanoffice.api.group.dto.CourseForGroupDTO;
import ua.edu.chdtu.deanoffice.api.group.dto.GroupDTO;
import ua.edu.chdtu.deanoffice.api.group.dto.GroupViews;
import ua.edu.chdtu.deanoffice.api.group.dto.GroupWithStudentsDTO;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.service.CourseForGroupService;
import ua.edu.chdtu.deanoffice.service.GroupService;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.GraduateService;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {
    @Autowired
    private GraduateService graduateService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private CourseForGroupService courseForGroupService;

    @RequestMapping(method = RequestMethod.GET, path = "/graduates")
    public ResponseEntity<List<GroupWithStudentsDTO>> getGraduateGroups(@RequestParam Integer degreeId) {
        List<StudentGroup> groups = graduateService.getGraduateGroups(degreeId);
        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<GroupWithStudentsDTO>>() {
        }.getType();
        List<GroupWithStudentsDTO> groupDTOs = modelMapper.map(groups, listType);
        return ResponseEntity.ok(groupDTOs);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @JsonView(GroupViews.Name.class)
    public List<GroupDTO> getGroups() {
        List<StudentGroup> studentGroups = groupService.getGroups();
        Type listType = new TypeToken<List<GroupDTO>>() {
        }.getType();
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(studentGroups, listType);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/year")
    public ResponseEntity<List<GroupWithStudentsDTO>> getGroupsByDegreeAndYear(@RequestParam Integer degreeId, @RequestParam Integer year) {
        List<StudentGroup> groups = groupService.getGroupsByDegreeAndYear(degreeId, year);
        ModelMapper modelMapper = new ModelMapper();
        Type listType = new TypeToken<List<GroupWithStudentsDTO>>() {}.getType();
        List<GroupWithStudentsDTO> groupDTOs = modelMapper.map(groups, listType);
        return ResponseEntity.ok(groupDTOs);
    }

    @RequestMapping("{id}/courses")
    @ResponseBody
    @JsonView(GroupViews.Course.class)
    public List<CourseForGroupDTO> getCourses(@PathVariable String id) {
        List<CourseForGroup> courseForGroups = courseForGroupService.getCourseForGroup(Integer.parseInt(id));
        Type listType = new TypeToken<List<CourseForGroupDTO>>() {
        }.getType();
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(courseForGroups, listType);
    }

    @RequestMapping("{id}/{semester}/courses")
    @ResponseBody
    @JsonView(GroupViews.Name.class)
    public List<CourseForGroupDTO> getCoursesBySemester(@PathVariable String id, @PathVariable String semester) {
        List<CourseForGroup> courseForGroups = courseForGroupService.getCourseForGroupBySemester(Integer.parseInt(id), Integer.parseInt(semester));
        Type listType = new TypeToken<List<CourseForGroupDTO>>() {
        }.getType();
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(courseForGroups, listType);
    }
}
