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