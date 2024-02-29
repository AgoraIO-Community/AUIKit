package io.agora.uikit.bean.valid;

import java.lang.reflect.Method;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumValid, Integer> {
    private EnumValid annotation;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        boolean result = false;

        Class<?> cls = annotation.value();
        boolean ignoreEmpty = annotation.ignoreEmpty();

        // The target is an enumeration, and the value is not empty or empty values are
        // not ignored before validation.
        if (cls.isEnum() && (value != null || !ignoreEmpty)) {

            Object[] objects = cls.getEnumConstants();
            try {
                Method method = cls.getMethod("getCode");

                for (Object obj : objects) {
                    Integer code = (Integer) method.invoke(obj);
                    if (code.equals(value)) {
                        result = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = true;
        }
        return result;
    }
}