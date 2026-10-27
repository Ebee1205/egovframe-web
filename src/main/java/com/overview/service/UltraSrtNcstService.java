package com.overview.service;

import java.util.List;
import java.util.Map;
import com.cmm.service.ComResponseVO;

public interface UltraSrtNcstService {

    // API 응답 검증 및 공통코드를 반영한 화면용 데이터 구성
    ComResponseVO<Map<String, Object>> fetchWeatherResponse() throws Exception;

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

