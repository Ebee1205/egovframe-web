package com.overview.service.impl;

import java.util.List;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;
import com.overview.service.UltraSrtNcstVO;

@Repository
public class UltraSrtNcstDAO extends EgovAbstractMapper {

    public void insertUltraSrtNcst(List<UltraSrtNcstVO> items) {
        insert("ultraSrtNcstDAO.insertUltraSrtNcst", items);
    }

    public List<UltraSrtNcstVO> selectUltraSrtNcstList(UltraSrtNcstVO condition) {
        return selectList("ultraSrtNcstDAO.selectUltraSrtNcstList", condition);
    }
}
