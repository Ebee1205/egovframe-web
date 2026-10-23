package com.overview.service;

import java.util.List;

public interface UltraSrtNcstService {

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

