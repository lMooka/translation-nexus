package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class TranslationKeyAlreadyExistsException extends BusinessException {

    private static final long serialVersionUID = 485762910384756L;
    public static final String CODE = "exception.translation.key-already-exists";
    public static final String MESSAGE = "Key '[keyCode]' already exists for version '[version]'.";

    public TranslationKeyAlreadyExistsException(String keyCode, String version) {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        param("keyCode", keyCode);
        param("version", version);
        setRetryable(false);
    }
}
