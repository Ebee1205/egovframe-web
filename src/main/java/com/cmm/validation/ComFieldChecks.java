package com.cmm.validation;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.springframework.validation.Errors;
import org.springmodules.validation.commons.FieldChecks;

public final class ComFieldChecks {
    private ComFieldChecks() { }

    public static boolean validateEnglishUpperCase(Object bean, ValidatorAction action, Field field, Errors errors) {
        return FieldChecks.validateMask(bean, action, field, errors);
    }
}
