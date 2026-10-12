package com.cmm.service.file;

import java.io.Serializable;
import java.util.Date;

/** Metadata corresponding to one tb_file row; storage paths are never client input. */
public class FileVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long fileId;
    private String fileStreCours;
    private String streFileNm;
    private String orignlFileNm;
    private String fileExtsn;
    private String fileCn;
    private Long fileSize;
    private Date cDate;
    private boolean imagesOnly;

    public Long getFileId() { 
    	return fileId; 
    }
    public void setFileId(Long value) {
    	fileId = value; 
    }
    public String getFileStreCours() { 
    	return fileStreCours; 
    }
    public void setFileStreCours(String value) { 
    	fileStreCours = value; 
    }
    public String getStreFileNm() { 
    	return streFileNm; 
	}
    public void setStreFileNm(String value) { 
    	streFileNm = value; 
    }
    public String getOrignlFileNm() { 
    	return orignlFileNm; 
    }
    public void setOrignlFileNm(String value) { 
    	orignlFileNm = value; 
    }
    public String getFileExtsn() { 
    	return fileExtsn; 
    }
    public void setFileExtsn(String value) { 
    	fileExtsn = value; 
    }
    public String getFileCn() { 
    	return fileCn; 
    }
    public void setFileCn(String value) { 
    	fileCn = value; 
    }
    public Long getFileSize() { 
    	return fileSize; 
    }
    public void setFileSize(Long value) { 
    	fileSize = value; 
    }
    public Date getCDate() { 
    	return cDate; 
    }
    public void setCDate(Date value) { 
    	cDate = value; 
    }
    public boolean isImagesOnly() { 
    	return imagesOnly; 
    }
    public void setImagesOnly(boolean value) { 
    	imagesOnly = value; 
    }
}
