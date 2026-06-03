package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class NoTranslationForLocaleException extends BusinessException {

    private static final long serialVersionUID = 789201938475629L;
    public static final String CODE = "exception.translation.locale-not-found";
    public static final String MESSAGE = "No translation for locale: [locale]";

    public NoTranslationForLocaleException(String locale) {
        super(NOT_FOUND_STATUS, CODE, MESSAGE);
        param("locale", locale);
        setRetryable(false);
    }
}
