package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class DuplicateRolesException extends BusinessException {

    private static final long serialVersionUID = 82736451293848L;
    public static final String CODE = "exception.user.duplicate-roles";
    public static final String MESSAGE = "Roles cannot contain duplicate values.";

    public DuplicateRolesException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }
}
