package com.alexhqi.saveshare.validation;

import com.alexhqi.saveshare.event.SuccessResult;

public interface Validator<T> {
    SuccessResult validate(T t);
}
