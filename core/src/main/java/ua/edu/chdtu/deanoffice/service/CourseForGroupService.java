package ua.edu.chdtu.deanoffice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;
import ua.edu.chdtu.deanoffice.repository.CourseForGroupRepository;

import java.util.List;

@Service
public class CourseForGroupService {
    private final CourseForGroupRepository courseForGroupRepository
            ;
    @Autowired
    public CourseForGroupService(CourseForGroupRepository courseForGroupRepository) {
        this.courseForGroupRepository = courseForGroupRepository;
    }

    public List<CourseForGroup> getCourseForGroup(int idGroup) {
        List<CourseForGroup> courseForGroup = courseForGroupRepository.findAllByStudentGroup(idGroup);
        return courseForGroup;
    }
}
