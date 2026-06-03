package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class UserAlreadyExistsException extends BusinessException {

    private static final long serialVersionUID = 182736451293847L;
    public static final String CODE = "exception.user.username-already-exists";
    public static final String MESSAGE = "Username already exists.";

    public UserAlreadyExistsException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
}
