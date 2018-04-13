package ua.edu.chdtu.deanoffice.entity;

import lombok.Getter;
import lombok.Setter;
import ua.edu.chdtu.deanoffice.entity.superclasses.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Getter
@Setter
public class Grade extends BaseEntity {
    @ManyToOne
    private Course course;
    @ManyToOne
    private StudentDegree studentDegree;
    @Column(name = "grade", nullable = false)
    private int grade;
    @Column(name = "points", nullable = false)
    private int points;
    @Column(name = "ects", length = 2)
    @Enumerated(value = EnumType.STRING)
    private EctsGrade ects;

    public String getNationalGradeUkr() {
        return ects.getNationalGradeUkr(this);
    }

    public String getNationalGradeEng() {
        return ects.getNationalGradeEng(this);
    }
}
