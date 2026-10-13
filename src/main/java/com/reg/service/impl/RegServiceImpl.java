package com.reg.service.impl;

import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reg.service.RegService;
import com.reg.service.RegFilterVO;
import com.reg.service.RegDetailVO;

@Service
public class RegServiceImpl implements RegService {
    private final RegDetailDAO regDAO;
    private final RegOptionDAO regOptionDAO;
    public RegServiceImpl(RegDetailDAO regDAO, RegOptionDAO regOptionDAO) {
        this.regDAO = regDAO;
        this.regOptionDAO = regOptionDAO;
    }
    @Override
    public RegDetailVO selectReg(RegDetailVO vo) throws Exception { return regDAO.selectReg(vo); }
    @Override
    public List<RegDetailVO> selectRegList(RegDetailVO vo) throws Exception {
        if (vo.getPageIndex() < 1) { vo.setPageIndex(1); }
        vo.setFirstIndex(vo.getPageSize() > 0 ? (vo.getPageIndex() - 1) * vo.getPageSize() : 0);
        vo.setTotalCnt(selectRegListCnt(vo));
        vo.setLastIndex(vo.getPageSize() > 0 ? vo.getFirstIndex() + vo.getPageSize() : vo.getTotalCnt());
        return regDAO.selectRegList(vo);
    }
    @Override
    public int selectRegListCnt(RegDetailVO vo) throws Exception { return regDAO.selectRegListCnt(vo); }
    @Override
    public List<RegFilterVO> selectRegDetail(RegFilterVO vo) throws Exception { return regOptionDAO.selectRegDetail(vo); }
    @Override
    public List<RegFilterVO> selectRegRoot() throws Exception { return regOptionDAO.selectRegRoot(new RegFilterVO()); }
    @Override
    public List<RegFilterVO> selectRegParents() throws Exception { return regOptionDAO.selectRegParents(new RegFilterVO()); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertReg(RegDetailVO vo) throws Exception {
        validateValues(vo);
        assignLevel(vo);
        checkDuplicate(vo);
        validateGeometry(vo);
        regDAO.insertReg(vo);
    }
    private void validateValues(RegDetailVO vo) {
        if (vo == null) { throw error(400, "REG_INVALID", "입력값을 확인해주세요.", null); }
        vo.setCode(vo.getCode() == null ? null : vo.getCode().trim());
        vo.setName(vo.getName() == null ? null : vo.getName().trim());
        if (vo.getCode() == null || vo.getCode().isEmpty() || vo.getCode().length() > 20) {
            throw error(400, "REG_CODE_INVALID", "행정구역 코드는 1~20자로 입력해주세요.", "code");
        }
        if (vo.getName() == null || vo.getName().isEmpty() || vo.getName().length() > 100) {
            throw error(400, "REG_NAME_INVALID", "행정구역명은 1~100자로 입력해주세요.", "name");
        }
        if (vo.getStatus() == null) { vo.setStatus("Y"); }
        if (!"Y".equals(vo.getStatus()) && !"N".equals(vo.getStatus())) {
            throw error(400, "REG_STATUS_INVALID", "상태는 사용 또는 미사용으로 지정해주세요.", "status");
        }
    }
    private void assignLevel(RegDetailVO vo) throws Exception {
        if (vo.getParentRegId() == null) { vo.setLevel(1); return; }
        if (vo.getParentRegId() <= 0 || vo.getParentRegId().equals(vo.getReg_id())) {
            throw error(400, "REG_PARENT_INVALID", "상위 행정구역 ID를 확인해주세요.", "parentRegId");
        }
        RegDetailVO query = new RegDetailVO(); query.setReg_id(vo.getParentRegId());
        RegDetailVO parent = regDAO.selectRegForUpdate(query);
        if (parent == null || !"Y".equals(parent.getStatus())) {
            throw error(400, "REG_PARENT_INVALID", "사용 중인 상위 행정구역을 선택해주세요.", "parentRegId");
        }
        vo.setLevel(parent.getLevel() + 1);
    }
    private void checkDuplicate(RegDetailVO vo) {
        if (regDAO.selectDuplicateRegCount(vo) > 0) {
            throw error(409, "REG_DUPLICATE", "이미 사용 중인 행정구역 코드입니다.", "code");
        }
    }
    private void validateGeometry(RegDetailVO vo) {
        if (vo.getGeom() == null || vo.getGeom().trim().isEmpty()) { vo.setGeom(null); return; }
        vo.setGeom(vo.getGeom().trim());
        boolean valid;
        try { valid = regDAO.isValidGeometry(vo); }
        catch (DataAccessException exception) {
            throw error(400, "REG_GEOM_INVALID", "영역을 올바른 Polygon 또는 MultiPolygon EWKT로 입력해주세요.", "geom");
        }
        if (!valid) {
            throw error(400, "REG_GEOM_INVALID", "영역은 비어 있지 않은 유효한 Polygon 또는 MultiPolygon이어야 합니다.", "geom");
        }
    }
    private RegException error(int status, String code, String message, String field) {
        return new RegException(status, code, message, field);
    }
}
