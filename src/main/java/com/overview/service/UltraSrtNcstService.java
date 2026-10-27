package com.overview.service;

import java.util.List;
import java.util.Map;
import com.cmm.service.ComResponseVO;

public interface UltraSrtNcstService {

    // 실황 누적 저장 (동일 발표분은 유지)
    void insertUltraSrtNcst(List<UltraSrtNcstVO> items) throws Exception;

    // API 응답 검증 후 저장할 실황 목록 반환 (저장은 호출부에서 수행)
    ComResponseVO<List<UltraSrtNcstVO>> fetchUltraSrtNcstRes() throws Exception;

    // 기본 격자의 최신 DB 자료를 공통코드와 매핑해 화면용 응답으로 반환
    ComResponseVO<Map<String, Object>> selectUltraSrtNcst() throws Exception;

    // API 오류 응답도 변형 없이 반환한다.
    String fetchUltraSrtNcstJson() throws Exception;

    // DB 저장 없이 최신 실황 API만 조회
    List<UltraSrtNcstVO> fetchUltraSrtNcst() throws Exception;

    // 실황 API 조회 후 DB 저장
    List<UltraSrtNcstVO> refreshUltraSrtNcst() throws Exception;
    List<UltraSrtNcstVO> refreshUltraSrtNcst(UltraSrtNcstVO condition) throws Exception;

    // 실황 단건 조회 (DB)
    UltraSrtNcstVO selectUltraSrtNcst(UltraSrtNcstVO condition) throws Exception;

    // 격자별 최신 발표분 전체 조회 (DB)
    List<UltraSrtNcstVO> selectUltraSrtNcstList(UltraSrtNcstVO condition) throws Exception;
}

