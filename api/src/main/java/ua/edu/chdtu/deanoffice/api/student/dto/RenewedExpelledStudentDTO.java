package ua.edu.chdtu.deanoffice.api.student.dto;

import lombok.Getter;
import lombok.Setter;
import ua.edu.chdtu.deanoffice.api.group.dto.StudentGroupDTO;
import ua.edu.chdtu.deanoffice.entity.Payment;

import java.util.Date;

@Getter
@Setter
public class RenewedExpelledStudentDTO {
    private Integer id;
    private StudentExpelDTO studentExpel;
    private int studyYear;
    private Payment payment;
    private StudentGroupDTO studentGroup;
    private Date renewDate;
    private Date applicationDate;
    private String academicCertificateNumber;
    private Date academicCertificateDate;
    private String academicCertificateIssuedBy;

    private Integer studentExpelId;
    private Integer studentGroupId;
}
