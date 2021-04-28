package com.alexhqi.saveshare.validation;

public interface Validator<T> {
    ValidationResult validate(T t);
}
