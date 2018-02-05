package ua.edu.chdtu.deanoffice.util;

import ua.edu.chdtu.deanoffice.entity.Grade;

public class GradeUtil {
    //TODO cr: перевести на енуми - так краще читати і більшість цих методів можна буде видалити
    public static String getECTSGrade(int points) {
        if (points >= 90 && points <= 100) return "A";
        if (points >= 82 && points <= 89) return "B";
        if (points >= 74 && points <= 81) return "C";
        if (points >= 64 && points <= 73) return "D";
        if (points >= 60 && points <= 63) return "E";
        if (points >= 35 && points <= 59) return "Fx";
        if (points >= 0 && points <= 34) return "F";
        else return "";
    }

    public static int getGradeFromPoints(double points) {
        if (points >= 90 && points <= 100) return 5;
        if (points >= 74 && points <= 89) return 4;
        if (points >= 60 && points <= 73) return 3;
        return 0;
    }

    public static int getMaxPointsFromGrade(double grade) {
        if (Math.round(grade) == 5) return 100;//impossible situation;
        if (Math.round(grade) == 4) return 89;
        if (Math.round(grade) == 3) return 73;
        return 0;
    }

    public static int getMinPointsFromGrade(double grade) {
        if (Math.round(grade) == 5) return 90;
        if (Math.round(grade) == 4) return 74;
        if (Math.round(grade) == 3) return 60;//impossible situation;
        return 0;
    }

    public static int getPointsFromGrade(Grade g) {
        if (Math.round(g.getGrade()) == 5) return 95;
        if (Math.round(g.getGrade()) == 4) return 82;
        if (Math.round(g.getGrade()) == 3) return 67;
        return 0;
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

    public static String getNationalGradeUkr(Grade g) {
        if (g.getCourse().getKnowledgeControl().isHasGrade()) {
            switch (g.getEcts()) {
                case "A":
                    return "Відмінно";
                case "B":
                    return "Добре";
                case "C":
                    return "Добре";
                case "D":
                    return "Задовільно";
                case "E":
                    return "Задовільно";
                case "F":
                    return "Незадовільно";
            }
        } else if (g.getEcts().equals("F") || g.getEcts().equals("Fx")) {
            return "Не зараховано";
        } else
            return "Зараховано";
        return "";
    }

    public static String getNationalGradeEng(Grade g) {
        if (g.getCourse().getKnowledgeControl().isHasGrade()) {
            switch (g.getEcts()) {
                case "A":
                    return "Excellent";
                case "B":
                    return "Good";
                case "C":
                    return "Good";
                case "D":
                    return "Satisfactory";
                case "E":
                    return "Satisfactory";
                case "F":
                    return "Fail";
            }
        } else if (g.getEcts().equals("F") || g.getEcts().equals("Fx")) {
            return "Fail";
        } else
            return "Passed";
        return "";
    }
}