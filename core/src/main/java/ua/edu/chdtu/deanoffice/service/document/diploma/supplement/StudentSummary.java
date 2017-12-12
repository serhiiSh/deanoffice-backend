package ua.edu.chdtu.deanoffice.service.document.diploma.supplement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.edu.chdtu.deanoffice.Constants;
import ua.edu.chdtu.deanoffice.entity.*;
import ua.edu.chdtu.deanoffice.util.GradeUtil;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StudentSummary {

    private static Logger log = LoggerFactory.getLogger(StudentSummary.class);

    private Student student;
    private List<List<Grade>> grades;
    private Integer totalHours = 0;

    public Integer getTotalHours() {
        return totalHours;
    }

    public List<List<Grade>> getGrades() {
        return grades;
    }

    public Student getStudent() {
        return student;
    }

    public StudentSummary(Student student, List<List<Grade>> grades) {
        this.student = student;
        this.grades = grades;
        completeGrades();
    }

    private void completeGrades() {
        setHours();
        setCredits();
        setGrades();
        setPoints();
        setECTS();
        combineMultipleSemesterCourseGrades();
    }

    private void setHours() {
        grades.forEach(gradeSublist -> {
            gradeSublist.forEach(grade -> {
                if (grade.getCourse().getHours() == null)
                    grade.getCourse().setHours(0);
                totalHours += grade.getCourse().getHours();
            });
        });
    }

    private void setCredits() {
        grades.forEach(gradeSublist -> {
            gradeSublist.forEach(grade -> {
                grade.getCourse().setCredits(new BigDecimal(grade.getCourse().getHours() / Constants.HOURS_PER_CREDIT));
            });
        });
    }

    private void setGrades() {
        grades.forEach(gradeSublist -> {
            gradeSublist.forEach(grade -> {
                if (grade.getGrade() == 0 && grade.getCourse().getKnowledgeControl().getId() != Constants.CREDIT)
                    grade.setGrade(GradeUtil.getGradeFromPoints(grade.getPoints()));
            });
        });
    }

    private void setPoints() {
        grades.forEach(gradeSublist -> {
            gradeSublist.forEach(grade -> {
                if (grade.getPoints() == 0)
                    grade.setPoints(GradeUtil.getPointsFromGrade(grade));
            });
        });
    }

    private void setECTS() {
        grades.forEach(gradeSublist -> {
            gradeSublist.forEach(grade -> {
                if (!"ABCDEFx".contains(grade.getEcts()))
                    grade.setEcts(GradeUtil.getECTSGrade(grade.getPoints()));
                if (grade.getCourse().getKnowledgeControl().getId() == Constants.CREDIT
                        && "ABCDE".contains(grade.getEcts().trim())) {
                    grade.setEcts("P");
                }
            });

        });
    }

    public static int[] adjustAverageGradeAndPoints(double averageGrade, double averagePoints) {
        int[] result = new int[2];
        if (Math.abs(averageGrade - 3.5) < 0.001 || Math.abs(averageGrade - 4.5) < 0.001) {
            result[1] = (int) Math.round(averagePoints);
            result[0] = GradeUtil.getGradeFromPoints(result[1]);
        } else {
            if (GradeUtil.getGradeFromPoints(averagePoints) == Math.round(averageGrade)) {
                result[0] = (int) Math.round(averageGrade);
                result[1] = (int) Math.round(averagePoints);
            }
            if (GradeUtil.getGradeFromPoints(averagePoints) > Math.round(averageGrade)) {
                result[0] = (int) Math.round(averageGrade);
                result[1] = GradeUtil.getMaxPointsFromGrade(averageGrade);
            }
            if (GradeUtil.getGradeFromPoints(averagePoints) < Math.round(averageGrade)) {
                result[0] = (int) Math.round(averageGrade);
                result[1] = GradeUtil.getMinPointsFromGrade(averageGrade);
            }
        }
        return result;
    }

    private void combineMultipleSemesterCourseGrades() {
        List<List<Grade>> gradesToCombine = new ArrayList<>();
        sortGradesByCourseNameUkr(this.grades.get(0));
        this.grades.get(0).forEach(grade -> {
            if (gradesToCombine.isEmpty()) {
                gradesToCombine.add(new ArrayList<>());
                gradesToCombine.get(0).add(grade);
            } else {
                if (gradesToCombine.get(gradesToCombine.size() - 1).stream()
                        .noneMatch(grade1 -> grade1.getCourse().getCourseName().equals(grade.getCourse().getCourseName())))
                    gradesToCombine.add(new ArrayList<>());
                gradesToCombine.get(gradesToCombine.size() - 1).add(grade);
            }
        });

        List<Grade> combinedGrades = new ArrayList<>();
        gradesToCombine.forEach(gradesList -> combinedGrades.add(combineGrades(gradesList)));
        this.grades.get(0).clear();
        this.grades.get(0).addAll(combinedGrades);
    }

    private void sortGradesByCourseNameUkr(List<Grade> grades) {
        grades.sort((o1, o2) -> {
            Collator ukrainianCollator = Collator.getInstance(new Locale("uk", "UA"));
            return ukrainianCollator.compare(o1.getCourse().getCourseName().getName(), o2.getCourse().getCourseName().getName());
        });
    }

    private Grade combineGrades(List<Grade> grades) {
        if (grades == null || grades.isEmpty())
            return null;
        Grade resultingGrade;
        Integer hoursSum = 0;
        for (Grade g : grades) {
            hoursSum += g.getCourse().getHours();
        }
        if (grades.size() == 1)
            return grades.get(0);
        else {
            List<Grade> examsGrades = getGradesByKnowledgeControlType(grades, Constants.EXAM);
            if (examsGrades.size() == 1)
                resultingGrade = examsGrades.get(0);
            else if (examsGrades.size() == 0) {
                List<Grade> differentiatedCreditGrades = getGradesByKnowledgeControlType(grades, Constants.DIFFERENTIATED_CREDIT);
                if (differentiatedCreditGrades.size() == 0)
                    resultingGrade = combineEqualGrades(grades);
                else if (differentiatedCreditGrades.size() == 1)
                    resultingGrade = differentiatedCreditGrades.get(0);
                else {
                    resultingGrade = combineEqualGrades(differentiatedCreditGrades);
                }
            } else {
                resultingGrade = combineEqualGrades(examsGrades);
            }
        }
        resultingGrade.getCourse().setHours(hoursSum);
        resultingGrade.getCourse().setCredits(new BigDecimal(hoursSum / Constants.HOURS_PER_CREDIT));
        return resultingGrade;
    }

    private Grade combineEqualGrades(List<Grade> grades) {
        Grade resultingGrade = grades.get(0);
        Double pointsSum = 0.0;
        Double gradesSum = 0.0;

        for (Grade g : grades) {
            pointsSum += g.getPoints();
            gradesSum += g.getGrade();
        }

        Course newCourse = new Course();
        newCourse.setHours(0);
        newCourse.setCourseName(resultingGrade.getCourse().getCourseName());
        newCourse.setKnowledgeControl(resultingGrade.getCourse().getKnowledgeControl());
        resultingGrade.setCourse(newCourse);

        if (resultingGrade.getCourse().getKnowledgeControl().getId() != Constants.CREDIT) {
            int[] pointsAndGrade = adjustAverageGradeAndPoints(
                    gradesSum / grades.size(),
                    pointsSum / grades.size());
            resultingGrade.setPoints(pointsAndGrade[1]);
            resultingGrade.setGrade(pointsAndGrade[0]);
            resultingGrade.setEcts(GradeUtil.getECTSGrade(resultingGrade.getPoints()));
        } else {
            resultingGrade.setPoints((int) Math.round(pointsSum / grades.size()));
            resultingGrade.setGrade((int) Math.round(gradesSum / grades.size()));
            if (resultingGrade.getPoints() >= 60)
                resultingGrade.setEcts("P");
            else
                resultingGrade.setEcts("F");
        }
        return resultingGrade;
    }


    private static List<Grade> getGradesByKnowledgeControlType(List<Grade> grades, Integer kcId) {
        return grades.stream().filter(grade -> grade.getCourse().getKnowledgeControl().getId() == kcId).collect(Collectors.toList());
    }

    public static Map<String, String> getGradeDictionary(Grade grade) {
        Map<String, String> result = new HashMap<>();
        try {
            result.put("#Credits", formatCredits(grade.getCourse().getCredits()));
            result.put("#Hours", String.format("%d", grade.getCourse().getHours()));
            result.put("#LocalGrade", String.format("%d", grade.getPoints()));
            result.put("#NationalGradeUkr", GradeUtil.getNationalGradeUkr(grade));
            result.put("#NationalGradeEng", GradeUtil.getNationalGradeEng(grade));
            result.put("#ECTSGrade", grade.getEcts());
            result.put("#CourseNameUkr", grade.getCourse().getCourseName().getName());
            result.put("#CourseNameEng", grade.getCourse().getCourseName().getNameEng());
        } catch (NullPointerException e) {
            log.warn("Some of grade's properties are null!");
        }
        return result;
    }

    public Map<String, String> getTotalDictionary() {
        Map<String, String> result = new HashMap<>();
        result.put("#TotalHours", String.format("%4d", getTotalHours()));
        result.put("#TotalCredits", formatCredits(getTotalCredits()));
        result.put("#TotalGrade", String.format("%2d", Math.round(getTotalGrade())));
        result.put("#TotalECTS", getTotalECTS());
        result.put("#TotalNGradeUkr", getTotalNationalGradeUkr());
        result.put("#TotalNGradeEng", getTotalNationalGradeEng());

        return result;
    }

    public Map<String, String> getStudentInfoDictionary() {
        Map<String, String> result = new HashMap<>();

        result.put("#SurnameUkr", student.getSurname() == null ? "" : student.getSurname().toUpperCase());
        result.put("#SurnameEng", student.getSurnameEng() == null ? "" : student.getSurnameEng().toUpperCase());
        result.put("#NameUkr", student.getName() == null ? "" : student.getName().toUpperCase());
        result.put("#NameEng", student.getNameEng() == null ? "" : student.getNameEng().toUpperCase());
        result.put("#PatronimicUkr", student.getPatronimic() == null ? "" : student.getPatronimic().toUpperCase());

        DateFormat dateOfBirthFormat = new SimpleDateFormat("dd.MM.yyyy");
        result.put("#BirthDate",
                student.getBirthDate() != null
                        ? dateOfBirthFormat.format(student.getBirthDate())
                        : "#BirthDate");

        String modeOfStudyUkr = "";
        String modeOfStudyEng = "";
        char modeOfStudy = student.getStudentGroup().getTuitionForm();
        if (modeOfStudy == 'f') {
            modeOfStudyUkr = "Денна";
            modeOfStudyEng = "Full-time";
        } else if (modeOfStudy == 'e') {
            modeOfStudyUkr = "Заочна";
            modeOfStudyEng = "Extramural";
        }
        result.put("#ModeOfStudyUkr", modeOfStudyUkr);
        result.put("#ModeOfStudyEng", modeOfStudyEng);

        Specialization specialization = student.getStudentGroup().getSpecialization();
        Speciality speciality = specialization.getSpeciality();
        Degree degree = specialization.getDegree();
        result.put("#SpecializationUkr", specialization.getName() == null ? "" : specialization.getName());
        result.put("#SpecializationEng", specialization.getNameEng() == null ? "" : specialization.getNameEng());
        result.put("#SpecialityUkr", speciality.getName() == null ? "" : speciality.getName());
        result.put("#SpecialityEng", speciality.getNameEng() == null ? "" : speciality.getNameEng());
        result.put("#DegreeUkr", degree.getName() == null ? "" : degree.getName());
        result.put("#DegreeEng", degree.getNameEng() == null ? "" : degree.getNameEng());
        result.put("#DEGREEUKR", degree.getName() == null ? "" : degree.getName().toUpperCase());
        result.put("#DEGREEENG", degree.getNameEng() == null ? "" : degree.getNameEng().toUpperCase());
        result.put("#QualificationUkr", specialization.getQualification() == null ? ""
                : specialization.getQualification());
        result.put("#QualificationEng", specialization.getQualificationEng() == null ? ""
                : specialization.getQualificationEng());

        try {
            StudentDegree studentDegree = student.getDegrees().stream().filter(
                    sd -> sd.getDegree().getName().equals(student.getStudentGroup().getSpecialization().getDegree().getName())
            ).findFirst().get();
            result.put("#ThesisNameUkr", studentDegree.getThesisName() == null ? ""
                    : studentDegree.getThesisName());
            result.put("#ThesisNameEng", studentDegree.getThesisNameEng() == null ? ""
                    : studentDegree.getThesisNameEng());
            result.put("#ProtocolNumber", studentDegree.getProtocolNumber() == null ? ""
                    : studentDegree.getProtocolNumber());
            result.put("#PreviousDiplomaNumber", studentDegree.getPreviousDiplomaNumber() == null ? ""
                    : studentDegree.getPreviousDiplomaNumber());

            int dateStyle = DateFormat.LONG;
            DateFormat protocolDateFormatUkr = DateFormat.getDateInstance(dateStyle, new Locale("uk", "UA"));
            result.put("#ProtocolDate", protocolDateFormatUkr.format(studentDegree.getProtocolDate()));
            DateFormat protocolDateFormatEng = DateFormat.getDateInstance(dateStyle, Locale.ENGLISH);
            result.put("#ProtocolDateEng", protocolDateFormatEng.format(studentDegree.getProtocolDate()));
        } catch (NoSuchElementException e) {
            log.warn("There is no suitable StudentDegree for this student");
        }

        return result;
    }

    public BigDecimal getTotalCredits() {
        return new BigDecimal(getTotalHours() / Constants.HOURS_PER_CREDIT);
    }

    private static String formatCredits(BigDecimal credits) {
        String formattedCredits = String.format("%.1f", credits);
        if (formattedCredits.split(",")[1].equals("0"))
            return String.format("%.0f", credits);
        else
            return formattedCredits;
    }

    public Double getTotalGrade() {
        int pointSum = 0;
        int pointsCount = 0;
        for (List<Grade> gradesSublist :
                grades) {
            for (Grade g : gradesSublist) {
                if (g.getCourse().getKnowledgeControl().getId() != Constants.CREDIT && g.getPoints() > 0) {
                    pointSum += g.getPoints();
                    pointsCount++;
                }
            }
        }
        if (pointsCount == 0)
            pointsCount = 1;
        return (pointSum * 1.0) / pointsCount;
    }

    public String getTotalNationalGradeUkr() {
        Grade g = new Grade();
        g.setEcts(getTotalECTS());
        Course c = new Course();
        g.setCourse(c);
        KnowledgeControl kc = new KnowledgeControl();
        kc.setHasGrade(true);
        c.setKnowledgeControl(kc);
        return GradeUtil.getNationalGradeUkr(g);
    }

    public String getTotalNationalGradeEng() {
        Grade g = new Grade();
        g.setEcts(getTotalECTS());
        Course c = new Course();
        g.setCourse(c);
        KnowledgeControl kc = new KnowledgeControl();
        kc.setHasGrade(true);
        c.setKnowledgeControl(kc);
        return GradeUtil.getNationalGradeEng(g);
    }

    private String getTotalECTS() {
        return GradeUtil.getECTSGrade((int) Math.round(getTotalGrade()));
    }
}
