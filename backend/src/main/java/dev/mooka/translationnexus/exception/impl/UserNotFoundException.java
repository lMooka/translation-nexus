package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    private static final long serialVersionUID = 283746591029384L;
    public static final String CODE = "exception.user.not-found";
    public static final String MESSAGE = "User not found.";

    public UserNotFoundException() {
        super(NOT_FOUND_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
}
