package com.cmm.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.ValidatorAction;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;
import org.springmodules.validation.commons.FieldChecks;

/**
 * 공통 커스텀 Validator.
 *
 * - validator-rules.xml 의 classname 으로 지정되는 클래스
 * - 반드시 public 클래스 + public 기본 생성자(암묵적 포함)
 * - 메서드 시그니처는 methodParams 와 정확히 일치해야 함
 *   (Object, ValidatorAction, Field, Errors) → boolean
 * - 날짜 형식은 var 'datePattern' 으로 지정 (기본 yyyy-MM-dd)
 * - 빈 값은 통과시킨다 (필수 여부는 required 가 담당)
 */
public class ComFieldChecks extends FieldChecks {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd";

    /**
     * 날짜 범위 검사: minDate <= 값 <= maxDate
     * vars: minDate, maxDate, datePattern(선택)
     */
    public boolean validateDateRange(Object bean, ValidatorAction va, Field field, Errors errors) {
        String pattern = getPattern(field);
        Object raw = getProperty(bean, field.getProperty());

        if (isBlank(raw)) {
            return true;
        }

        Date value = toDate(raw, pattern);
        Date min = toDate(field.getVarValue("minDate"), pattern);
        Date max = toDate(field.getVarValue("maxDate"), pattern);

        if (value == null || min == null || max == null || value.before(min) || value.after(max)) {
            reject(errors, field, va);
            return false;
        }
        return true;
    }

    /**
     * 날짜 비교: 현재 필드 >= compareProperty 필드
     * (예: 종료일 필드에 compareProperty=sDate)
     * vars: compareProperty, datePattern(선택)
     */
    public boolean validateDateAfterOrEqual(Object bean, ValidatorAction va, Field field, Errors errors) {
        String pattern = getPattern(field);
        Object raw = getProperty(bean, field.getProperty());
        Object compareRaw = getProperty(bean, field.getVarValue("compareProperty"));

        if (isBlank(raw) || isBlank(compareRaw)) {
            return true;
        }

        Date value = toDate(raw, pattern);
        Date compare = toDate(compareRaw, pattern);

        if (value == null || compare == null || value.before(compare)) {
            reject(errors, field, va);
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static String getPattern(Field field) {
        String pattern = field.getVarValue("datePattern");
        return GenericValidator.isBlankOrNull(pattern) ? DEFAULT_PATTERN : pattern;
    }

    private static Object getProperty(Object bean, String property) {
        if (bean == null || property == null) {
            return null;
        }
        try {
            return PropertyUtils.getProperty(bean, property);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isBlank(Object value) {
        return value == null || (value instanceof String && ((String) value).trim().length() == 0);
    }

    /** Date 타입이면 그대로, 문자열이면 pattern 으로 엄격하게 파싱. 실패 시 null */
    private static Date toDate(Object value, String pattern) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        String text = value.toString().trim();
        if (text.length() == 0) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);
        ParsePosition pos = new ParsePosition(0);
        Date date = format.parse(text, pos);
        if (date == null || pos.getIndex() != text.length()) {
            return null;
        }
        return date;
    }

    /**
     * 오류 등록. validation.xml 의 <msg> 가 있으면 그 키, 없으면 validator 의 msg 키 사용.
     * <arg> 가 resource="true"(기본) 이면 메시지 키로 해석되어 필드명 등으로 치환된다.
     */
    private static void reject(Errors errors, Field field, ValidatorAction va) {
        String code = field.getMsg(va.getName());
        if (code == null) {
            code = va.getMsg();
        }

        List<Object> args = new ArrayList<Object>();
        Arg[] fieldArgs = field.getArgs(va.getName());
        if (fieldArgs != null) {
            for (Arg arg : fieldArgs) {
                if (arg == null) {
                    args.add("");
                } else if (arg.isResource()) {
                    args.add(new DefaultMessageSourceResolvable(new String[] { arg.getKey() }, arg.getKey()));
                } else {
                    args.add(arg.getKey());
                }
            }
        }

        errors.rejectValue(field.getKey(), code, args.toArray(), code);
    }
}
