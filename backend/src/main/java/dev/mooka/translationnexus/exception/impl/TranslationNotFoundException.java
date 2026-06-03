package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class TranslationNotFoundException extends BusinessException {

    private static final long serialVersionUID = 586729103847562L;
    public static final String CODE = "exception.translation.not-found";
    public static final String MESSAGE = "Translation key not found.";

    public TranslationNotFoundException() {
        super(NOT_FOUND_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
}
