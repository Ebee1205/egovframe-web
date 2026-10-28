package com.overview.service;

import java.util.List;
import java.util.Map;
import com.cmm.service.ComResponseVO;

public interface UltraSrtNcstService {

    // API 조회 및 응답 검증
    ComResponseVO<List<UltraSrtNcstVO>> fetchUltraSrtNcstRes(int nx, int ny) throws Exception;

    // 실황 누적 저장 (동일 발표분은 유지)
    void insertUltraSrtNcst(List<UltraSrtNcstVO> items) throws Exception;

    // 해당 격자의 최신 DB 자료를 공통코드와 매핑해 화면용 응답으로 반환
    ComResponseVO<Map<String, Object>> selectUltraSrtNcst(int nx, int ny) throws Exception;
}
