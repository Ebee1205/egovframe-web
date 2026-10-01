package com.cmm.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/** 검증 오류를 필드명별 메시지와 JSON 응답 본문으로 변환한다. */
public final class ErrMessageBuildUtil {

    private ErrMessageBuildUtil() { }

    /** 같은 필드의 오류가 여러 개일 수 있으므로 메시지를 목록으로 보존한다. */
    public static Map<String, List<String>> buildFieldMessages(
            Errors errors, MessageSource messageSource, Locale locale) {
        Map<String, List<String>> messages = new LinkedHashMap<>();
        for (FieldError error : errors.getFieldErrors()) {
            messages.computeIfAbsent(error.getField(), field -> new ArrayList<>())
                    .add(messageSource.getMessage(error, locale));
        }
        return messages;
    }

    /** errors에는 필드별 오류, message에는 특정 필드에 속하지 않는 오류를 담는다. */
    public static String build(Errors errors, MessageSource messageSource, Locale locale) {
        JsonObjectBuilder fields = Json.createObjectBuilder();
        buildFieldMessages(errors, messageSource, locale).forEach((field, messages) -> {
            JsonArrayBuilder values = Json.createArrayBuilder();
            messages.forEach(values::add);
            fields.add(field, values);
        });

        StringJoiner message = new StringJoiner("\n");
        for (ObjectError error : errors.getGlobalErrors()) {
            message.add(messageSource.getMessage(error, locale));
        }
        return Json.createObjectBuilder().add("errors", fields)
                .add("message", message.toString()).build().toString();
    }
}
