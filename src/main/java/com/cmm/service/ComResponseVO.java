package com.cmm.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

/** Common JSON response and localized validation error conversion. */
public final class ComResponseVO<T> {
    private final T data;
    private final Error error;
    private final Code code;

    private ComResponseVO(T data, List<FieldError> fields, String resultCode, String message) {
        this.data = data;
        this.error = new Error(fields);
        this.code = new Code(resultCode, message);
    }

    public static <T> ComResponseVO<T> success(T data, String message) {
        return new ComResponseVO<>(data, Collections.emptyList(), "SUCCESS", message);
    }

    public static <T> ComResponseVO<T> fail(String resultCode, String message) {                            
        return new ComResponseVO<>(null, Collections.emptyList(), resultCode, message);
    }

    /** Failure for a single field without requiring a Spring BindingResult. */
    public static <T> ComResponseVO<T> fieldFailure(String resultCode, String message,
            String field, String errorCode, String fieldMessage) {
        return new ComResponseVO<>(null,
                Collections.singletonList(new FieldError(field, errorCode, fieldMessage)), resultCode, message);
    }
    
    /** Preserve every field error, including multiple errors on the same field. */
    public static ComResponseVO<Void> validationFailure(Errors errors, MessageSource messageSource,
            Locale locale, String resultCode, String defaultMessage) {
        List<FieldError> fields = new ArrayList<>();
        for (org.springframework.validation.FieldError fieldError : errors.getFieldErrors()) {
            fields.add(new FieldError(fieldError.getField(), fieldError.getCode(),
                    messageSource.getMessage(fieldError, locale)));
        }
        StringJoiner globalMessages = new StringJoiner("\n");
        for (ObjectError objectError : errors.getGlobalErrors()) {
            globalMessages.add(messageSource.getMessage(objectError, locale));
        }
        String message = globalMessages.length() == 0 ? defaultMessage : globalMessages.toString();
        return new ComResponseVO<>(null, fields, resultCode, message);
    }

    /** Field-to-messages mapping retained from ErrMessageBuildUtil. */
    public static Map<String, List<String>> buildFieldMessages(
            Errors errors, MessageSource messageSource, Locale locale) {
        Map<String, List<String>> messages = new LinkedHashMap<>();
        for (org.springframework.validation.FieldError fieldError : errors.getFieldErrors()) {
            messages.computeIfAbsent(fieldError.getField(), field -> new ArrayList<>())
                    .add(messageSource.getMessage(fieldError, locale));
        }
        return messages;
    }

    /** Keep the three top-level fields even when data or message is null. */
    public String toJson() {
        try (Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withNullValues(true))) {
            return jsonb.toJson(this);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize common response.", exception);
        }
    }

    public T getData() { return data; }
    public Error getError() { return error; }
    public Code getCode() { return code; }

    public static final class Error {
        private final List<FieldError> fields;
        private Error(List<FieldError> fields) {
            this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        }
        public List<FieldError> getFields() { return fields; }
    }

    public static final class FieldError {
        private final String field;
        private final String errorCode;
        private final String message;
        private FieldError(String field, String errorCode, String message) {
            this.field = field;
            this.errorCode = errorCode;
            this.message = message;
        }
        public String getField() { return field; }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }

    public static final class Code {
        private final String resultCode;
        private final String message;
        private Code(String resultCode, String message) {
            this.resultCode = resultCode;
            this.message = message;
        }
        public String getResultCode() { return resultCode; }
        public String getMessage() { return message; }
    }
}
