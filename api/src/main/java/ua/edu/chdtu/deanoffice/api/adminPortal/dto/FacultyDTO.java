package ua.edu.chdtu.deanoffice.api.adminPortal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacultyDTO {
    private long id;
    private String name;
    private String abbr;
    private String dean;
}
