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

    /** 신규 데이터의 c_date는 DB 현재 시각으로 입력하고 중복 갱신 시 유지한다. */
    public void upsertUltraSrtNcstList(List<UltraSrtNcstVO> items) {
        insert("ultraSrtNcstDAO.upsertUltraSrtNcstList", items);
    }

    public UltraSrtNcstVO selectUltraSrtNcst(UltraSrtNcstVO condition) {
        return selectOne("ultraSrtNcstDAO.selectUltraSrtNcst", condition);
    }

    public List<UltraSrtNcstVO> selectUltraSrtNcstList(UltraSrtNcstVO condition) {
        return selectList("ultraSrtNcstDAO.selectUltraSrtNcstList", condition);
    }
}
