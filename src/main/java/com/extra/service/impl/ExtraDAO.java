package com.extra.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.extra.service.ExtraVO;
import com.extra.service.ExtraFilterVO;

@Repository
public class ExtraDAO extends EgovAbstractMapper {

    private static final int INSERT_CHUNK_SIZE = 1000;

    public int insertExtra(ExtraVO item) {
        if (item == null) throw new IllegalArgumentException("등록할 대여이력이 필요합니다.");
        return insert("extraDAO.insertExtra", item);
    }

    /** PostgreSQL 바인딩 파라미터 수를 제한하며, 전체 목록을 한 트랜잭션으로 등록한다. */
    @Transactional(rollbackFor = Exception.class)
    public int insertExtraList(List<ExtraVO> items) {
        if (items == null || items.isEmpty()) return 0;
        for (ExtraVO item : items) {
            if (item == null) throw new IllegalArgumentException("대여이력 목록에 빈 행이 있습니다.");
        }
        int inserted = 0;
        for (int start = 0; start < items.size(); start += INSERT_CHUNK_SIZE) {
            int end = Math.min(start + INSERT_CHUNK_SIZE, items.size());
            inserted += insert("extraDAO.insertExtraList", items.subList(start, end));
        }
        return inserted;
    }

    /** 자전거ID, 대여소번호, 반납대여소번호 및 페이징 조건으로 조회한다. */
    public List<ExtraVO> selectExtraList(ExtraFilterVO condition) {
        return selectList("extraDAO.selectExtraList", condition == null ? new ExtraFilterVO() : condition);
    }

    public long selectExtraListCnt(ExtraFilterVO condition) {
        return selectOne("extraDAO.selectExtraListCnt", condition == null ? new ExtraFilterVO() : condition);
    }
}
