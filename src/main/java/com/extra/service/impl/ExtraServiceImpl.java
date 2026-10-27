package com.extra.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.extra.service.ExtraFilterVO;
import com.extra.service.ExtraService;
import com.extra.service.ExtraVO;

@Service
public class ExtraServiceImpl implements ExtraService {
    private final ExtraDAO extraDAO;

    public ExtraServiceImpl(ExtraDAO extraDAO) {
        this.extraDAO = extraDAO;
    }

    @Override
    public List<ExtraVO> selectExtraList(ExtraFilterVO filterVO) {
        filterVO.setPageIndex(Math.max(1, filterVO.getPageIndex()));
        if (filterVO.getPageSize() < 0) filterVO.setPageSize(10);
        int total = Math.toIntExact(extraDAO.selectExtraListCnt(filterVO));
        filterVO.setTotalCnt(total);
        if (filterVO.getPageSize() > 0) {
            int totalPage = total == 0 ? 1 : (total - 1) / filterVO.getPageSize() + 1;
            filterVO.setPageIndex(Math.min(filterVO.getPageIndex(), totalPage));
            filterVO.setFirstIndex((filterVO.getPageIndex() - 1) * filterVO.getPageSize());
            filterVO.setLastIndex((int) Math.min((long) filterVO.getFirstIndex() + filterVO.getPageSize(), total));
        } else {
            filterVO.setFirstIndex(0);
            filterVO.setLastIndex(total);
        }
        return extraDAO.selectExtraList(filterVO);
    }
}
