# 행정구역 관리

- 목록: `/region/list.do`, 상세: `GET /region/detail.do?rid=...`
- 등록/수정: `POST /region/insert.do`, `POST /region/update.do`
- 삭제: `POST /region/delete.do` (`rid`), 처리 후 목록으로 이동
- 필드: `rid`, `parentRegId` (`p_reg_id`), `code` (최대 20자, 전체 유일), `name` (최대 100자), `level`, `geom`, `status`, `cDate`, `uDate`.
- `level`은 최상위 1, 부모 레벨 + 1로 자동 계산한다. 상위 행정구역은 여러 단계에서 선택할 수 있다.
- 하위 행정구역이 있으면 삭제와 부모 변경을 제한한다. 자기 자신을 부모로 지정할 수 없다.
- `geom`은 선택 입력인 EWKT 문자열이다. PostGIS에서 Polygon/MultiPolygon 및 유효성을 검사하며 조회 시 SRID를 보존한다. 빈 입력은 NULL로 저장한다.
- 예: `SRID=4326;POLYGON((127 37,128 37,128 38,127 37))`. 좌표계는 입력값을 따른다.
- `c_date`, `u_date`는 등록 시 서버 DB 시각으로 설정하고, 수정 시 `u_date`를 갱신한다.
- 제공된 `tb_reg` 테이블과 PostGIS 확장이 DB에 준비되어 있어야 한다. 이 변경은 DB DDL을 실행하지 않는다.
