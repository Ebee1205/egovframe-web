package com.cmm.service.excel;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import javax.servlet.http.HttpServletResponse;
import com.cmm.service.excel.impl.ExcelDomFileWriter;
import com.cmm.service.file.impl.FileMngUtil;

@Component
@Primary
public class ExcelFileWriter {

    private final ExcelTemplate excelTemplate;

    public ExcelFileWriter() { this(new ExcelTemplate()); }

    @org.springframework.beans.factory.annotation.Autowired
    public ExcelFileWriter(ExcelTemplate excelTemplate) {
        this.excelTemplate = excelTemplate;
    }

    /** XML 양식을 읽어 엑셀 파일을 생성한다. */
    public byte[] writeXml(Object data, String classpathXml) throws IOException {
        return writeXml(data, classpathXml, null);
    }

    public byte[] writeXml(Object data, String classpathXml, ExcelStyle style) throws IOException {
        return writeMapped(data, excelTemplate.load(classpathXml), style);
    }

    /** 이미 해석한 동일한 양식을 Parser와 공유할 수 있다. */
    public byte[] writeMapped(Object data, ExcelTemplate.Definition template, ExcelStyle style) throws IOException {
        return new ExcelDomFileWriter().write(data, template, style);
    }

    public byte[] writeXmlTemplate(String classpathXml, ExcelStyle style) throws IOException {
        return writeXml(null, classpathXml, style);
    }

    public void downloadXml(HttpServletResponse response, String fileName, Object data,
            String classpathXml, ExcelStyle style) throws IOException {
        byte[] content = writeXml(data, classpathXml, style);
        FileMngUtil.download(response, fileName, FileMngUtil.contentType("xlsx"), content);
    }

}
