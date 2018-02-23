package ua.edu.chdtu.deanoffice.service.document;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class DocumentIOService {

    public WordprocessingMLPackage loadTemplate(String name) throws IOException, Docx4JException {
        return WordprocessingMLPackage.load(new FileInputStream(new ClassPathResource(name).getFile()));
    }

    public File saveDocumentToTemp(WordprocessingMLPackage template, String fileName) throws Docx4JException {
        File documentFile = new File(System.getProperty("java.io.tmpdir") + fileName);
        template.save(documentFile);
        return documentFile;
    }

}