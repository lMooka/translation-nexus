package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    private static final long serialVersionUID = 384756291038475L;
    public static final String CODE = "exception.auth.invalid-credentials";
    public static final String MESSAGE = "Invalid credentials.";

    public InvalidCredentialsException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
}
