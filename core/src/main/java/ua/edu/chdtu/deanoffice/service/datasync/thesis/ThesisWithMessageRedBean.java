package ua.edu.chdtu.deanoffice.service.datasync.thesis;

import lombok.Getter;

@Getter
public class ThesisWithMessageRedBean {
    private ThesisDataBean thesisPrimaryData;
    private String message;

    public ThesisWithMessageRedBean(){}
    public ThesisWithMessageRedBean(String message, ThesisDataBean thesisPrimaryData){
        this.message = message;
        this.thesisPrimaryData = thesisPrimaryData;
    }
}
