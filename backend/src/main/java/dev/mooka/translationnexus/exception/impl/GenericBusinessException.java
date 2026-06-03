package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class GenericBusinessException extends BusinessException {

    private static final long serialVersionUID = 99887766554433L;

    public GenericBusinessException(String message) {
        super(DEFAULT_STATUS, "exception.generic", message);
        setRetryable(false);
    }

    public GenericBusinessException(Integer status, String code, String message) {
        super(status, code, message);
        setRetryable(false);
    }
}
