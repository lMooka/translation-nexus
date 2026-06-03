package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class DuplicateAllowedLocalesException extends BusinessException {

    private static final long serialVersionUID = 82736451293849L;
    public static final String CODE = "exception.user.duplicate-allowed-locales";
    public static final String MESSAGE = "Allowed locales cannot contain duplicate values.";

    public DuplicateAllowedLocalesException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
}
