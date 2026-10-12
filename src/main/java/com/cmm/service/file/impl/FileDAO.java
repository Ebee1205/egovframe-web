package com.cmm.service.file.impl;

import java.util.List;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;
import com.cmm.service.file.FileVO;

@Repository("fileDAO")
public class FileDAO extends EgovAbstractMapper {
    public FileVO selectFileInf(Long fileId) { return selectOne("fileDAO.selectFileInf", fileId); }
    public FileVO selectFileForUpdate(Long fileId) { return selectOne("fileDAO.selectFileForUpdate", fileId); }
    public List<FileVO> selectFileInfs(FileVO filter) { return selectList("fileDAO.selectFileInfs", filter); }
    public void insertFileInf(FileVO file) { insert("fileDAO.insertFileInf", file); }
    public int updateFileInf(FileVO file) { return update("fileDAO.updateFileInf", file); }
    public int deleteFileInf(Long fileId) { return delete("fileDAO.deleteFileInf", fileId); }
}
