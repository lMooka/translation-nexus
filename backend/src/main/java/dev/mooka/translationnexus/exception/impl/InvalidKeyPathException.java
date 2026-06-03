package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class InvalidKeyPathException extends BusinessException {

    private static final long serialVersionUID = 582739485726L;
    public static final String CODE = "exception.category.invalid-path";
    public static final String MESSAGE = "Key code does not match category path configurations.";

    public InvalidKeyPathException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }

    public InvalidKeyPathException(String keyCode, String category) {
        super(DEFAULT_STATUS, CODE, "Key code '" + keyCode + "' does not match any allowed paths defined for category '" + category + "'.");
        setRetryable(false);
    }
}
