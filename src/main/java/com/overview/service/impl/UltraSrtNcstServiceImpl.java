package com.overview.service.impl;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.cmm.service.ComResponseVO;
import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.overview.service.UltraSrtNcstService;
import com.overview.service.UltraSrtNcstVO;

@Service
public class UltraSrtNcstServiceImpl implements UltraSrtNcstService {

    private final UltraSrtNcstDAO dao;
    private final CodeService codeService;
    private final String serviceKey;
    private final String endpoint;
    private final RestTemplate restTemplate;

    public UltraSrtNcstServiceImpl(UltraSrtNcstDAO dao, CodeService codeService,
            @Value("${Globals.dataGo.serviceKey}") String serviceKey,
            @Value("${Globals.dataGo.Endpoint}") String endpoint) {
    	
        this.dao = dao;
        this.codeService = codeService;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public void insertUltraSrtNcst(List<UltraSrtNcstVO> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return;
        }
        dao.insertUltraSrtNcst(items);
    }

    @Override
    public ComResponseVO<List<UltraSrtNcstVO>> fetchUltraSrtNcstRes(Integer Nx, Integer Ny) throws Exception {
        // 요청 조건 → API 호출 → 결과 확인 → 항목 변환/검증
        UltraSrtNcstVO req = latestCondition();
        
        req.setNx(Nx == null ? 57 : Nx);
        req.setNy(Ny == null ? 127 : Ny);

        String resJson;
        
        try {
            resJson = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(endpoint)
                    .queryParam("serviceKey", "{serviceKey}")
                    .queryParam("numOfRows", 1000).queryParam("pageNo", 1)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", req.getBaseDate())
                    .queryParam("base_time", req.getBaseTime())
                    .queryParam("nx", req.getNx()).queryParam("ny", req.getNy())
                    .encode().buildAndExpand(serviceKey).toUri(),
                String.class);
            
        } catch (RestClientResponseException ex) {
        	
            if (!ex.getResponseBodyAsString().trim().isEmpty()) {
                resJson = ex.getResponseBodyAsString();
            } else {
                throw new IllegalStateException("기상청 API HTTP 오류: " + ex.getRawStatusCode());
            }

        } catch (RestClientException ex) {
            Throwable cause = ex.getMostSpecificCause();
            throw new IllegalStateException("기상청 API 통신 실패: " + cause.getClass().getSimpleName());
        }
        if (resJson == null || resJson.trim().isEmpty()) {
            throw new IllegalStateException("기상청 API 응답이 비어 있습니다.");
        }
        JsonObject resField;
        try (JsonReader resReader = Json.createReader(new StringReader(resJson))) {
            resField = resReader.readObject().getJsonObject("response");
        }
        JsonObject resHeader = resField.getJsonObject("header");
        String resCode = resHeader.getString("resultCode");
        String resMsg = resHeader.getString("resultMsg");
        if (!"00".equals(resCode)) {
            return ComResponseVO.fail(resCode, resMsg);
        }

        JsonArray resItems = resField.getJsonObject("body").getJsonObject("items").getJsonArray("item");
        
        List<UltraSrtNcstVO> observations = new ArrayList<>();
        
        for (JsonValue resValue : resItems) {
        	
            JsonObject resItem = (JsonObject) resValue;
            
            UltraSrtNcstVO observation = new UltraSrtNcstVO();
            
            observation.setBaseDate(scalarText(resItem, "baseDate"));
            observation.setBaseTime(scalarText(resItem, "baseTime"));
            observation.setNx(Integer.valueOf(scalarText(resItem, "nx")));
            observation.setNy(Integer.valueOf(scalarText(resItem, "ny")));
            observation.setCategory(scalarText(resItem, "category"));
            observation.setObsrValue(scalarText(resItem, "obsrValue"));
            observations.add(observation);
        }
        
        return ComResponseVO.success(observations, resMsg);
    }

    @Override
    public ComResponseVO<Map<String, Object>> selectUltraSrtNcst(Integer Nx, Integer Ny) throws Exception {
        UltraSrtNcstVO req = new UltraSrtNcstVO();
        req.setNx(Nx == null ? 57 : Nx);
        req.setNy(Ny == null ? 127 : Ny);
        List<UltraSrtNcstVO> rows = dao.selectUltraSrtNcstList(req);
        return buildWeatherResponse(rows.toArray(new UltraSrtNcstVO[0]), null);
    }

    private ComResponseVO<Map<String, Object>> buildWeatherResponse(
            UltraSrtNcstVO[] observations, String resMsg) throws Exception {
        UltraSrtNcstVO first = observations != null && observations.length > 0 ? observations[0] : null;
        List<Map<String, Object>> values = new ArrayList<>();

        if (first != null) {
            // 1. 항목명·단위·설명·강수형태 코드 조회
            Map<String, String> categoryNames = weatherCodeNames("NCST_CTG_ROOT");
            Map<String, String> units = weatherCodeNames("NCST_UNT_ROOT");
            Map<String, String> descriptions = weatherCodeNames("NCST_DSC_ROOT");

            CodeFilterVO precipitationFilter = new CodeFilterVO();
            precipitationFilter.setParentCode("NCST_CTG_PTY");
            Map<String, CodeFilterVO> precipitationCodes = new LinkedHashMap<>();
            for (CodeFilterVO code : codeService.selectCmmCodeDetail(precipitationFilter)) {
                precipitationCodes.put(code.getCode(), code);
            }

            for (UltraSrtNcstVO observation : observations) {
                if (!java.util.Objects.equals(first.getBaseDate(), observation.getBaseDate())
                        || !java.util.Objects.equals(first.getBaseTime(), observation.getBaseTime())
                        || !java.util.Objects.equals(first.getNx(), observation.getNx())
                        || !java.util.Objects.equals(first.getNy(), observation.getNy())) {
                    throw new IllegalStateException("실황 자료의 발표일시 또는 격자가 일치하지 않습니다.");
                }

                // 2. 항목별 표시 데이터 구성
                values.add(toWeatherItem(observation, categoryNames, units, descriptions, precipitationCodes));
            }
        }

        // 3. 공통 정보와 항목 목록을 마지막에 등록
        Map<String, Object> data = new LinkedHashMap<>();
        if (first != null) {
            data.put("baseDate", first.getBaseDate());
            data.put("baseTime", first.getBaseTime());
            data.put("nx", first.getNx());
            data.put("ny", first.getNy());
        }
        data.put("items", values);
        return ComResponseVO.success(data, resMsg);
    }

    private Map<String, Object> toWeatherItem(UltraSrtNcstVO observation,
            Map<String, String> categoryNames, Map<String, String> units,
            Map<String, String> descriptions, Map<String, CodeFilterVO> precipitationCodes) {
        // 코드 매핑 및 값 변환
        String category = observation.getCategory();
        BigDecimal obsrValue = observation.getObsrValue() == null ? null : new BigDecimal(observation.getObsrValue());
        String categoryName = categoryNames.getOrDefault("NCST_CTG_" + category, category);
        String unit = units.getOrDefault("NCST_UNT_" + category, "");
        String description = descriptions.getOrDefault("NCST_DSC_" + category, "");
        CodeFilterVO precipitation = null;
        
        if ("PTY".equals(category) && obsrValue != null) {
            String suffix = obsrValue.stripTrailingZeros().toPlainString();
            precipitation = precipitationCodes.get("NCST_CTG_PTY_" + suffix);
        }

        // item 하위 응답 필드
        Map<String, Object> value = new LinkedHashMap<>();

        value.put("category", category);
        value.put("obsrValue", obsrValue);
        value.put("categoryName", categoryName);
        value.put("unit", unit);
        value.put("description", description);

        if (precipitation != null) {
            value.put("valueName", precipitation.getName());
            value.put("emoji", precipitation.getDsc());
        }
        return value;
    }

    private Map<String, String> weatherCodeNames(String parentCode) throws Exception {
        CodeFilterVO filter = new CodeFilterVO();
        filter.setParentCode(parentCode);
        Map<String, String> names = new LinkedHashMap<>();
        for (CodeFilterVO code : codeService.selectCmmCodeDetail(filter)) {
            names.put(code.getCode(), code.getName());
        }
        return names;
    }

    private UltraSrtNcstVO latestCondition() {
        // 정시 발표 자료의 제공 지연을 고려해 40분 전의 시각을 사용한다.
        ZonedDateTime available = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(40);
        UltraSrtNcstVO req = new UltraSrtNcstVO();
        req.setBaseDate(available.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
        req.setBaseTime(available.format(DateTimeFormatter.ofPattern("HH")) + "00");
        return req;
    }

    private String scalarText(JsonObject object, String name) {
        JsonValue value = object.get(name);
        if (value instanceof JsonString) {
            return ((JsonString) value).getString();
        }
        if (value != null && value.getValueType() == JsonValue.ValueType.NUMBER) {
            return value.toString();
        }
        throw new IllegalStateException("기상청 API 응답 필드 누락 또는 형식 오류: " + name);
    }

    
}
