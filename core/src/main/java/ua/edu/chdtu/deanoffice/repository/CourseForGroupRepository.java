package ua.edu.chdtu.deanoffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;

import java.util.List;

/**
 * Created by os199 on 06.11.2017.
 */
public interface CourseForGroupRepository extends JpaRepository<CourseForGroup, Integer> {

    @Query("select cfg.course from CourseForGroup as cfg " +
            "join cfg.studentGroup sg join sg.specialization spec " +
            "where sg.active = 'T' and spec.id = :specId")
    List<CourseForGroup> findAllBySpecialization(@Param("specId") int specId);

}