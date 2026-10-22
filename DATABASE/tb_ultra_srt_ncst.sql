-- PostgreSQL: 초단기실황 API 응답 items.item 저장용.
-- API 설정과 응답 메타정보는 저장하지 않는다.
-- nx, ny는 호출에 사용한 격자 좌표를 명시적으로 입력한다.

CREATE TABLE public.tb_ultra_srt_ncst (
    base_date  varchar(8)  NOT NULL,
    base_time  varchar(4)  NOT NULL,
    nx         integer     NOT NULL,
    ny         integer     NOT NULL,
    category   varchar(10) NOT NULL,
    obsr_value varchar(100) NOT NULL,
    CONSTRAINT tb_ultra_srt_ncst_pkey
        PRIMARY KEY (nx, ny, base_date, base_time, category),
    CONSTRAINT tb_ultra_srt_ncst_base_date_check
        CHECK (base_date ~ '^[0-9]{8}$'),
    CONSTRAINT tb_ultra_srt_ncst_base_time_check
        CHECK (base_time ~ '^([01][0-9]|2[0-3])00$'),
    CONSTRAINT tb_ultra_srt_ncst_grid_check
        CHECK (nx > 0 AND ny > 0)
);

COMMENT ON TABLE public.tb_ultra_srt_ncst IS '기상청 초단기실황';
COMMENT ON COLUMN public.tb_ultra_srt_ncst.base_date IS 'baseDate: 발표일자 (YYYYMMDD, KST)';
COMMENT ON COLUMN public.tb_ultra_srt_ncst.base_time IS 'baseTime: 발표시각 (HHmm, KST)';
COMMENT ON COLUMN public.tb_ultra_srt_ncst.nx IS 'nx: 예보지점 X 격자 좌표';
COMMENT ON COLUMN public.tb_ultra_srt_ncst.ny IS 'ny: 예보지점 Y 격자 좌표';
COMMENT ON COLUMN public.tb_ultra_srt_ncst.category IS 'category: 자료구분코드';
COMMENT ON COLUMN public.tb_ultra_srt_ncst.obsr_value IS 'obsrValue: 실황값 (API 원문 문자열)';

-- MyBatis 입력 예시 (아래 구문은 매퍼 XML에서 사용).
-- INSERT INTO public.tb_ultra_srt_ncst
--     (base_date, base_time, nx, ny, category, obsr_value)
-- VALUES
--     (#{baseDate}, #{baseTime}, #{nx}, #{ny}, #{category}, #{obsrValue})
-- ON CONFLICT (nx, ny, base_date, base_time, category)
-- DO UPDATE SET obsr_value = EXCLUDED.obsr_value;
