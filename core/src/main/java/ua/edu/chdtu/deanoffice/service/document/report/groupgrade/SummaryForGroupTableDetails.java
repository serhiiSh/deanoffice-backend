package ua.edu.chdtu.deanoffice.service.document.report.groupgrade;

import lombok.Getter;
import ua.edu.chdtu.deanoffice.entity.Grade;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SummaryForGroupTableDetails {

    private List<StudentSummaryForGroup> studentSummaries;

    private int rowWithNamesPosition = 0;
    private int rowWithTotalGradesPosition = rowWithNamesPosition + 1;
    private int rowWithAverageGradePosition = rowWithTotalGradesPosition + 1;
    private int rowWithDiplomaGradePosition = rowWithAverageGradePosition + 1;
    private int rowWithGeneralGradesStarts = rowWithDiplomaGradePosition + 1;
    private int rowWithGeneralGradesEnds;
    private int rowWithCourseWorksStarts;
    private int rowWithCourseWorksEnds;
    private int rowWithPracticesStarts;
    private int rowWithPracticesEnds;
    private int rowWithHoursPosition;
    private int rowWithCreditsPosition;

    public SummaryForGroupTableDetails(List<StudentSummaryForGroup> studentSummaries) {
        this.studentSummaries = studentSummaries;
        List<Grade> generalGrades = new ArrayList<>(studentSummaries.get(0).getGrades().get(0));
        List<Grade> courseWorks = new ArrayList<>(studentSummaries.get(0).getGrades().get(1));
        List<Grade> practices = new ArrayList<>(studentSummaries.get(0).getGrades().get(2));
        List<Grade> diplomaGrades = new ArrayList<>(studentSummaries.get(0).getGrades().get(3));
        rowWithGeneralGradesEnds = rowWithGeneralGradesStarts + generalGrades.size() - 1;
        rowWithCourseWorksStarts = rowWithGeneralGradesEnds + 2;
        rowWithCourseWorksEnds = rowWithCourseWorksStarts + courseWorks.size() - 1;
        rowWithPracticesStarts = rowWithCourseWorksEnds + 2;
        rowWithPracticesEnds = rowWithPracticesStarts + practices.size() - 1;
        rowWithHoursPosition = rowWithPracticesEnds + 1;
        rowWithCreditsPosition = rowWithHoursPosition + 1;
    }

}