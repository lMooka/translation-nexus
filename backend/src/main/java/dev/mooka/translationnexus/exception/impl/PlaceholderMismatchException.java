package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class PlaceholderMismatchException extends BusinessException {

    private static final long serialVersionUID = 687291038475629L;
    public static final String CODE = "exception.translation.placeholder-mismatch";
    public static final String MESSAGE = "Placeholder mismatch. Required: [requiredPlaceholders]";

    public PlaceholderMismatchException(String requiredPlaceholders) {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        param("requiredPlaceholders", requiredPlaceholders);
        setRetryable(false);
    }
}
