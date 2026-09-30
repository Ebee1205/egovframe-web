-- ============================================================
-- SEED DATA
-- ============================================================

-- 1. COMMON CODE - ROOT
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (1, NULL, 'STORE_CTG_ROOT', '음식점 카테고리', 1, '음식점 분류', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (2, NULL, 'EVENT_CTG_ROOT', '이벤트 카테고리', 1, '이벤트 분류', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (3, NULL, 'TAG_ROOT', '태그', 1, '공통 태그', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (4, NULL, 'USER_TYPE_ROOT', '사용자 유형', 1, '사용자 권한 유형', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (5, NULL, 'USER_STAT_ROOT', '사용자 상태', 1, '사용자 상태', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (6, NULL, 'STORE_STAT_ROOT', '음식점 상태', 1, '음식점 상태', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (7, NULL, 'EVENT_STAT_ROOT', '이벤트 상태', 1, '이벤트 상태', 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');

-- 2. COMMON CODE - STORE CATEGORY
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (8, 1, 'STORE_CTG_KOREAN', '한식', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (9, 1, 'STORE_CTG_CHINESE', '중식', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (10, 1, 'STORE_CTG_JAPANESE', '일식', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (11, 1, 'STORE_CTG_WESTERN', '양식', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (12, 1, 'STORE_CTG_CAFE', '카페', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');

-- 3. COMMON CODE - EVENT CATEGORY
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (20, 2, 'EVENT_CTG_FESTIVAL', '축제', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (21, 2, 'EVENT_CTG_MARKET', '플리마켓', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (22, 2, 'EVENT_CTG_EXHIBIT', '전시', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (23, 2, 'EVENT_CTG_MEETING', '모임', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (24, 2, 'EVENT_CTG_PERFORMANCE', '공연', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');

-- 4. COMMON CODE - TAG
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (30, 3, 'TAG_DATE', '데이트', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (31, 3, 'TAG_FAMILY', '가족', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (32, 3, 'TAG_PET', '반려동물', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (33, 3, 'TAG_PHOTO', '사진명소', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (34, 3, 'TAG_LOCAL', '지역행사', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (35, 3, 'TAG_FOOD', '먹거리', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');

-- 5. COMMON CODE - USER TYPE / STATUS
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (40, 4, 'USER_TYPE_ADMIN', '관리자', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (41, 4, 'USER_TYPE_USER', '일반 사용자', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (50, 5, 'USER_STAT_ACTIVE', '활성', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (51, 5, 'USER_STAT_INACTIVE', '비활성', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (52, 5, 'USER_STAT_BLOCKED', '차단', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');

-- 6. COMMON CODE - STORE / EVENT STATUS
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (60, 6, 'STORE_STAT_ACTIVE', '운영중', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (61, 6, 'STORE_STAT_INACTIVE', '운영중지', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (70, 7, 'EVENT_STAT_READY', '예정', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (71, 7, 'EVENT_STAT_OPEN', '진행중', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (72, 7, 'EVENT_STAT_CLOSED', '종료', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_cmn_code (cid, p_cid, code, name, c_lvl, dsc, status, c_date, u_date) VALUES (73, 7, 'EVENT_STAT_CANCELLED', '취소', 2, NULL, 'Y', '2026-09-28 15:00:00', '2026-09-28 15:00:00');


-- ============================================================
-- REGIONS
-- ============================================================
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(1, NULL, 'SEOUL', '서울특별시', 1, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(2, 1, 'SEOUL_JONGNO', '종로구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(3, 1, 'SEOUL_JUNG', '중구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(4, 1, 'SEOUL_YONGSAN', '용산구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(5, 1, 'SEOUL_SEONGDONG', '성동구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(6, 1, 'SEOUL_GWANGJIN', '광진구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(7, 1, 'SEOUL_DONGDAEMUN', '동대문구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(8, 1, 'SEOUL_JUNGNANG', '중랑구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(9, 1, 'SEOUL_SEONGBUK', '성북구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(10, 1, 'SEOUL_GANGBUK', '강북구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(11, 1, 'SEOUL_DOBONG', '도봉구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(12, 1, 'SEOUL_NOWON', '노원구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(13, 1, 'SEOUL_EUNPYEONG', '은평구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(14, 1, 'SEOUL_SEODAEMUN', '서대문구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(15, 1, 'SEOUL_MAPO', '마포구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(16, 1, 'SEOUL_YANGCHEON', '양천구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(17, 1, 'SEOUL_GANGSEO', '강서구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(18, 1, 'SEOUL_GURO', '구로구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(19, 1, 'SEOUL_GEUMCHEON', '금천구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(20, 1, 'SEOUL_YEONGDEUNGPO', '영등포구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(21, 1, 'SEOUL_DONGJAK', '동작구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(22, 1, 'SEOUL_GWANAK', '관악구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(23, 1, 'SEOUL_SEOCHO', '서초구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(24, 1, 'SEOUL_GANGNAM', '강남구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(25, 1, 'SEOUL_SONGPA', '송파구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');
INSERT INTO tb_reg (rid, p_reg_id, code, "name", "level", geom, status, c_date, u_date) VALUES(26, 1, 'SEOUL_GANGDONG', '강동구', 2, NULL, 'Y', '2026-09-28 15:00:00.000', '2026-09-28 15:00:00.000');



-- ============================================================
-- USER
-- ============================================================

INSERT INTO tb_users (uid, email, pwd, nickname, type, status, rid, c_date, u_date) VALUES (1, 'admin@test.com', 'test1234', '관리자', 'USER_TYPE_ADMIN', 'USER_STAT_ACTIVE', NULL, '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_users (uid, email, pwd, nickname, type, status, rid, c_date, u_date) VALUES (2, 'user01@test.com', 'test1234', '영도주민', 'USER_TYPE_USER', 'USER_STAT_ACTIVE', NULL, '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_users (uid, email, pwd, nickname, type, status, rid, c_date, u_date) VALUES (3, 'user02@test.com', 'test1234', '부산여행자', 'USER_TYPE_USER', 'USER_STAT_ACTIVE', NULL, '2026-09-28 15:00:00', '2026-09-28 15:00:00');
INSERT INTO tb_users (uid, email, pwd, nickname, type, status, rid, c_date, u_date) VALUES (4, 'user03@test.com', 'test1234', '산책러', 'USER_TYPE_USER', 'USER_STAT_ACTIVE', NULL, '2026-09-28 15:00:00', '2026-09-28 15:00:00');

-- ============================================================
-- IDENTITY SEQUENCE SYNC
-- ============================================================

SELECT setval(pg_get_serial_sequence('tb_cmn_code', 'cid'), (SELECT MAX(cid) FROM tb_cmn_code));
SELECT setval(pg_get_serial_sequence('tb_users', 'uid'), (SELECT MAX(uid) FROM tb_users));
SELECT setval(pg_get_serial_sequence('tb_cmn_code', 'cid'), (SELECT MAX(cid) FROM tb_cmn_code));

COMMIT;