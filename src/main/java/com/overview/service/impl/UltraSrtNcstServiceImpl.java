package com.overview.service.impl;

import java.io.StringReader;
import java.net.URI;
import java.math.BigDecimal;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import com.cmm.service.ComResponseVO;
import com.code.service.CodeService;
import com.code.service.CodeFilterVO;
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
import javax.json.JsonValue;
import javax.json.JsonString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.overview.service.UltraSrtNcstService;
import com.overview.service.UltraSrtNcstVO;

@Service
public class UltraSrtNcstServiceImpl implements UltraSrtNcstService {

    private final UltraSrtNcstDAO dao;
    private final CodeService codeService;
    private final String serviceKey;
    private final String endpoint;
    private final int defaultNx;
    private final int defaultNy;
    private final RestTemplate restTemplate;

    public UltraSrtNcstServiceImpl(UltraSrtNcstDAO dao, CodeService codeService,
            @Value("${Globals.dataGo.serviceKey}") String serviceKey,
            @Value("${Globals.dataGo.Endpoint}") String endpoint,
            @Value("${Globals.dataGo.nx:57}") int defaultNx,
            @Value("${Globals.dataGo.ny:127}") int defaultNy) {
        this.dao = dao;
        this.codeService = codeService;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
        this.defaultNx = defaultNx;
        this.defaultNy = defaultNy;
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
    public ComResponseVO<List<UltraSrtNcstVO>> fetchUltraSrtNcstRes() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {

            // API응답 가져오기
            Map<?, ?> root = jsonb.fromJson(fetchUltraSrtNcstJson(), Map.class);
            Map<?, ?> response = (Map<?, ?>) root.get("response");
            Map<?, ?> header = (Map<?, ?>) response.get("header");
            String resultCode = (String) header.get("resultCode");
            String resultMsg = (String) header.get("resultMsg");
            if (resultCode == null || resultMsg == null) {
                throw new IllegalStateException("기상청 API 응답 헤더 누락");
            }
            if (!"00".equals(resultCode)) {
                return ComResponseVO.fail(resultCode, resultMsg);
            }

            Map<?, ?> body = (Map<?, ?>) response.get("body");
            Map<?, ?> items = (Map<?, ?>) body.get("items");
            UltraSrtNcstVO[] observations = jsonb.fromJson(jsonb.toJson(items.get("item")), UltraSrtNcstVO[].class);
            List<UltraSrtNcstVO> rows = observations == null ? new ArrayList<UltraSrtNcstVO>()
                    : java.util.Arrays.asList(observations);
            for (UltraSrtNcstVO row : rows) {
                validateDateTime(row);
                if (row.getNx() == null || row.getNy() == null || row.getNx() < 1 || row.getNy() < 1
                        || row.getCategory() == null || row.getCategory().trim().isEmpty()
                        || row.getObsrValue() == null) {
                    throw new IllegalStateException("실황 자료의 필수 항목이 누락되었습니다.");
                }
                new BigDecimal(row.getObsrValue());
                UltraSrtNcstVO first = rows.get(0);
                if (!java.util.Objects.equals(first.getBaseDate(), row.getBaseDate())
                        || !java.util.Objects.equals(first.getBaseTime(), row.getBaseTime())
                        || !java.util.Objects.equals(first.getNx(), row.getNx())
                        || !java.util.Objects.equals(first.getNy(), row.getNy())) {
                    throw new IllegalStateException("실황 자료의 발표일시 또는 격자가 일치하지 않습니다.");
                }
            }
            return ComResponseVO.success(rows, resultMsg);
        }
    }

    @Override
    public ComResponseVO<Map<String, Object>> selectUltraSrtNcst() throws Exception {
        List<UltraSrtNcstVO> rows = selectUltraSrtNcstList(null);
        return buildWeatherResponse(rows.toArray(new UltraSrtNcstVO[0]), null);
    }

    @Override
    public String fetchUltraSrtNcstJson() throws Exception {
        if (serviceKey == null || serviceKey.trim().isEmpty()) {
            throw new IllegalStateException("기상청 API 인증키가 설정되지 않았습니다.");
        }
        return requestJson(withGrid(latestCondition()), 1);
    }

    @Override
    public List<UltraSrtNcstVO> fetchUltraSrtNcst() throws Exception {
        if (serviceKey == null || serviceKey.trim().isEmpty()) {
            throw new IllegalStateException("기상청 API 인증키가 설정되지 않았습니다.");
        }
        return fetchItems(withGrid(latestCondition()));
    }

    @Override
    public List<UltraSrtNcstVO> refreshUltraSrtNcst() throws Exception {
        return refreshUltraSrtNcst(latestCondition());
    }

    @Override
    public List<UltraSrtNcstVO> refreshUltraSrtNcst(UltraSrtNcstVO condition) throws Exception {
        UltraSrtNcstVO query = withGrid(condition);
        validateDateTime(query);
        if (serviceKey == null || serviceKey.trim().isEmpty()) {
            throw new IllegalStateException("기상청 API 인증키가 설정되지 않았습니다.");
        }
        List<UltraSrtNcstVO> items = fetchItems(query);
        // 하나의 INSERT 문으로 전체 자료를 저장하여 부분 저장을 방지한다.
        dao.upsertUltraSrtNcstList(items);
        return items;
    }

    @Override
    public UltraSrtNcstVO selectUltraSrtNcst(UltraSrtNcstVO condition) throws Exception {
        UltraSrtNcstVO query = withGrid(condition);
        validateDateTime(query);
        if (query.getCategory() == null || query.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("자료구분코드가 필요합니다.");
        }
        return dao.selectUltraSrtNcst(query);
    }

    @Override
    public List<UltraSrtNcstVO> selectUltraSrtNcstList(UltraSrtNcstVO condition) throws Exception {
        return dao.selectUltraSrtNcstList(withGrid(condition));
    }

    private ComResponseVO<Map<String, Object>> buildWeatherResponse(
            UltraSrtNcstVO[] observations, String resultMsg) throws Exception {
        UltraSrtNcstVO first = observations != null && observations.length > 0 ? observations[0] : null;
        List<Map<String, Object>> values = new ArrayList<>();

        if (first != null) {
            // 공통코드 맵핑
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

                // 하위 항목 공통코드~~
                String category = observation.getCategory();
                BigDecimal obsrValue = observation.getObsrValue() == null
                        ? null : new BigDecimal(observation.getObsrValue());
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
                values.add(value);
            }
        }

        // data 필드
        Map<String, Object> data = new LinkedHashMap<>();
        if (first != null) {
            data.put("baseDate", first.getBaseDate());
            data.put("baseTime", first.getBaseTime());
            data.put("nx", first.getNx());
            data.put("ny", first.getNy());
        }
        data.put("items", values);
        return ComResponseVO.success(data, resultMsg);
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
        UltraSrtNcstVO condition = new UltraSrtNcstVO();
        condition.setBaseDate(available.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
        condition.setBaseTime(available.format(DateTimeFormatter.ofPattern("HH")) + "00");
        return condition;
    }

    private UltraSrtNcstVO withGrid(UltraSrtNcstVO source) {
        UltraSrtNcstVO query = new UltraSrtNcstVO();
        query.setNx(source == null || source.getNx() == null ? defaultNx : source.getNx());
        query.setNy(source == null || source.getNy() == null ? defaultNy : source.getNy());
        if (query.getNx() < 1 || query.getNy() < 1) {
            throw new IllegalArgumentException("격자 좌표는 양수여야 합니다.");
        }
        if (source != null) {
            query.setBaseDate(source.getBaseDate());
            query.setBaseTime(source.getBaseTime());
            query.setCategory(source.getCategory());
        }
        return query;
    }

    private void validateDateTime(UltraSrtNcstVO query) {
        if (query.getBaseDate() == null || !query.getBaseDate().matches("[0-9]{8}")
                || query.getBaseTime() == null || !query.getBaseTime().matches("([01][0-9]|2[0-3])00")) {
            throw new IllegalArgumentException("발표일자는 YYYYMMDD, 발표시각은 HH00 형식이어야 합니다.");
        }
        LocalDate.parse(query.getBaseDate(), DateTimeFormatter.BASIC_ISO_DATE);
    }

    private String requestJson(UltraSrtNcstVO query, int page) {
            URI uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                    .queryParam("serviceKey", "{serviceKey}")
                    .queryParam("numOfRows", 1000).queryParam("pageNo", page)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", query.getBaseDate())
                    .queryParam("base_time", query.getBaseTime())
                    .queryParam("nx", query.getNx()).queryParam("ny", query.getNy())
                    .encode().buildAndExpand(serviceKey).toUri();
            String json;
            try {
                json = restTemplate.getForObject(uri, String.class);
            } catch (RestClientResponseException ex) {
                // HTTP 오류에도 API 응답 본문이 있으면 코드/메시지 확인을 위해 전달한다.
                if (!ex.getResponseBodyAsString().trim().isEmpty()) {
                    return ex.getResponseBodyAsString();
                }
                throw new IllegalStateException("기상청 API HTTP 오류: " + ex.getRawStatusCode());
            } catch (RestClientException ex) {
                // HTTP 예외에는 인증키가 포함된 URL이 들어갈 수 있어 전달하지 않는다.
                Throwable cause = ex.getMostSpecificCause();
                throw new IllegalStateException("기상청 API 통신 실패: " + cause.getClass().getSimpleName());
            }
            if (json == null || json.trim().isEmpty()) {
                throw new IllegalStateException("기상청 API 응답이 비어 있습니다.");
            }
            return json;
    }

    private List<UltraSrtNcstVO> fetchItems(UltraSrtNcstVO query) {
        Map<String, UltraSrtNcstVO> items = new LinkedHashMap<String, UltraSrtNcstVO>();
        int page = 1;
        int received = 0;
        int total;
        do {
            String json = requestJson(query, page);
            try (JsonReader reader = Json.createReader(new StringReader(json))) {
                JsonObject response = reader.readObject().getJsonObject("response");
                String code = response.getJsonObject("header").getString("resultCode");
                if (!"00".equals(code)) {
                    throw new IllegalStateException("기상청 API 조회 실패 (결과코드: "
                            + (code.matches("[A-Za-z0-9_-]{1,20}") ? code : "UNKNOWN") + ")");
                }
                JsonObject body = response.getJsonObject("body");
                total = body.getInt("totalCount");
                JsonArray rows = body.getJsonObject("items").getJsonArray("item");
                if (rows.isEmpty() || total < 1 || total > 10000) {
                    throw new IllegalStateException("기상청 실황 자료가 없거나 응답 건수가 올바르지 않습니다.");
                }
                for (JsonValue row : rows) {
                    JsonObject value = (JsonObject) row;
                    UltraSrtNcstVO item = new UltraSrtNcstVO();
                    item.setBaseDate(scalarText(value, "baseDate"));
                    item.setBaseTime(scalarText(value, "baseTime"));
                    item.setNx(Integer.valueOf(scalarText(value, "nx")));
                    item.setNy(Integer.valueOf(scalarText(value, "ny")));
                    item.setCategory(scalarText(value, "category"));
                    item.setObsrValue(scalarText(value, "obsrValue"));
                    if (!query.getBaseDate().equals(item.getBaseDate())
                            || !query.getBaseTime().equals(item.getBaseTime())
                            || !query.getNx().equals(item.getNx()) || !query.getNy().equals(item.getNy())
                            || item.getCategory().trim().isEmpty()) {
                        throw new IllegalStateException("요청 조건과 기상청 실황 자료가 일치하지 않습니다.");
                    }
                    items.put(item.getCategory(), item);
                }
                received += rows.size();
                page++;
            } catch (javax.json.JsonException | NullPointerException | ClassCastException | NumberFormatException ex) {
                throw new IllegalStateException("기상청 API 응답 형식 오류: " + ex.getClass().getSimpleName());
            }
        } while (received < total);
        return new ArrayList<UltraSrtNcstVO>(items.values());
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
