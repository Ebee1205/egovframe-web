package com.reg.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
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
    public RegDetailVO selectRegDetailMap(RegDetailVO vo) throws Exception { return regDAO.selectRegDetailMap(vo); }
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
    public List<RegFilterVO> selectCmmRegDetail(RegFilterVO vo) throws Exception { return regOptionDAO.selectCmmRegDetail(vo); }
    @Override
    public List<RegFilterVO> selectRegDetail(RegFilterVO vo) throws Exception { return selectCmmRegDetail(vo); }
    @Override
    public List<RegFilterVO> selectRegRoot() throws Exception { return selectRegRoot(new RegFilterVO()); }
    @Override
    public List<RegFilterVO> selectRegRoot(RegFilterVO vo) throws Exception { return regOptionDAO.selectRegRoot(vo); }
    @Override
    public List<RegFilterVO> selectRegParents() throws Exception { return regOptionDAO.selectRegParents(new RegFilterVO()); }
}
