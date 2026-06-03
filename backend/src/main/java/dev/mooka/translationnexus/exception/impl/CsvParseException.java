package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class CsvParseException extends BusinessException {

    private static final long serialVersionUID = 890123847562910L;
    public static final String CODE = "exception.translation.csv-parse-failed";
    public static final String MESSAGE = "Failed to parse CSV: [error]";

    public CsvParseException(String error) {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        param("error", error);
        setRetryable(false);
    }
}
