package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class VersionLockedException extends BusinessException {

    private static final long serialVersionUID = 77112233445566L;
    public static final String CODE = "exception.version.locked";
    public static final String MESSAGE = "This version is locked and cannot be modified.";

    public VersionLockedException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
    
    public VersionLockedException(String customMessage) {
        super(DEFAULT_STATUS, CODE, customMessage);
        setRetryable(false);
    }
}
