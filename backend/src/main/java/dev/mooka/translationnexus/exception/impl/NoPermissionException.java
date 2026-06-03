package dev.mooka.translationnexus.exception.impl;


import dev.mooka.translationnexus.exception.BusinessException;

public class NoPermissionException extends BusinessException {

  private static final long serialVersionUID = 5136006282124243844L;
  //@formatter:off
  public static final String CODE = "exception.permission.no-permission";
  public static final String MESSAGE = "You don't have permission to perform this action.";
  //@formatter:on

  public NoPermissionException() {
    super(DEFAULT_STATUS, CODE, MESSAGE);
    setRetryable(false);
  }
}
