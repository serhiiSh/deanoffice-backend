package ua.edu.chdtu.deanoffice.service.document.report.journal;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.edu.chdtu.deanoffice.entity.CourseForGroup;
import ua.edu.chdtu.deanoffice.entity.StudentGroup;
import ua.edu.chdtu.deanoffice.service.CourseForGroupService;
import ua.edu.chdtu.deanoffice.service.GradeService;
import ua.edu.chdtu.deanoffice.service.StudentGroupService;
import ua.edu.chdtu.deanoffice.service.document.DocumentIOService;
import ua.edu.chdtu.deanoffice.service.document.FileFormatEnum;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.DiplomaSupplementService;
import ua.edu.chdtu.deanoffice.util.LanguageUtil;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static ua.edu.chdtu.deanoffice.service.document.TemplateUtil.*;

@Service
public class ReportsCoursesService {

    private static final String TEMPLATES_PATH = "docs/templates/";
    private static final String TEMPLATE = TEMPLATES_PATH + "PredmJourn.docx";
    private static Logger log = LoggerFactory.getLogger(DiplomaSupplementService.class);

    private GradeService gradeService;
    private StudentGroupService groupService;
    private CourseForGroupService courseForGroupService;
    private DocumentIOService documentIOService;

    public ReportsCoursesService(GradeService gradeService, StudentGroupService groupService, DocumentIOService documentIOService, CourseForGroupService courseForGroupService) {
        this.gradeService = gradeService;
        this.groupService = groupService;
        this.courseForGroupService = courseForGroupService;
        this.documentIOService = documentIOService;
    }

    public synchronized File prepareReportForGroup(Integer groupId,Integer semesterId) throws Docx4JException, IOException {
        StudentGroup group = groupService.getById(groupId);
        return documentIOService.saveDocumentToTemp(fillTemplate(TEMPLATE, prepareGroup(groupId,semesterId),group.getName()), LanguageUtil.transliterate(group.getName())+".docx", FileFormatEnum.DOCX);
    }
    public synchronized File prepareReportForYear(Integer year,Integer semesterId) throws Docx4JException, IOException {
        List<StudentGroup> studentGroups = groupService.getGroupsByYear(year);
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        for(StudentGroup groups:studentGroups){
            List<CourseReport> courseReports = prepareGroup(groups.getId(),(int)semesterId);
            if(studentGroups.get(0) == groups){
                 wordMLPackage = fillTemplate(TEMPLATE, courseReports,groups.getName());
            }
            else {
                wordMLPackage.getMainDocumentPart().getContent().addAll(fillTemplate(TEMPLATE, courseReports,groups.getName()).getMainDocumentPart().getContent());
            }
        }
        return documentIOService.saveDocumentToTemp(wordMLPackage, "jurnal_vidom_bakalavr_"+year+"_kurs"+".docx", FileFormatEnum.DOCX);
    }

    private List<CourseReport> prepareGroup(Integer groupId,Integer semesterId) {
        List<CourseReport> courseReports = new ArrayList<>();
        List<CourseForGroup> courseForGroups = courseForGroupService.getCoursesForGroupBySemester((int)groupId,(int)semesterId);
        Format formatter = new SimpleDateFormat("yyyy.MM.dd");
        for(CourseForGroup courseForGroup:courseForGroups){
            courseReports.add(new CourseReport(courseForGroup.getCourse().getCourseName().getName(),
                    courseForGroup.getCourse().getHours().toString(),
                    courseForGroup.getTeacher().getSurname()+" "
                            +courseForGroup.getTeacher().getName().charAt(0)+"."
                            +courseForGroup.getTeacher().getPatronimic().charAt(0)+".",
                    courseForGroup.getExamDate() == null ? "" : formatter.format(courseForGroup.getExamDate())));
        }
        return courseReports;
    }



    private WordprocessingMLPackage fillTemplate(String templateName, List<CourseReport> courseReports, String groupName) throws IOException, Docx4JException {
        WordprocessingMLPackage template = documentIOService.loadTemplate(templateName);
        fillTableWithGrades(template, courseReports);
        Map<String, String> commonDict = new HashMap<>();
        commonDict.put("GroupName", groupName);
        replaceTextPlaceholdersInTemplate(template, commonDict);
        return template;
    }

    private void fillTableWithGrades(WordprocessingMLPackage template, List<CourseReport> courseReports) {
        List<Object> tables = getAllElementsFromObject(template.getMainDocumentPart(), Tbl.class);
        String tableWithGradesKey = "#Pred";
        Tbl tempTable = findTable(tables, tableWithGradesKey);
        if (tempTable == null) {
            log.warn("Couldn't find table that contains: " + tableWithGradesKey);
            return;
        }
        List<Object> gradeTableRows = getAllElementsFromObject(tempTable, Tr.class);

        Tr templateRow = (Tr) gradeTableRows.get(0);
        int rowToAddIndex = 1;
        for (CourseReport report : courseReports) {
            Map<String, String> replacements = report.getDictionary();
            addRowToTable(tempTable, templateRow, rowToAddIndex, replacements);
            rowToAddIndex++;
        }
        tempTable.getContent().remove(templateRow);
    }


}
