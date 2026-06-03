package dev.mooka.translationnexus.exception.impl;

import dev.mooka.translationnexus.exception.BusinessException;

public class CategoryNotFoundException extends BusinessException {

    private static final long serialVersionUID = 12948756302834L;
    public static final String CODE = "exception.category.not-found";
    public static final String MESSAGE = "Category not found.";

    public CategoryNotFoundException() {
        super(DEFAULT_STATUS, CODE, MESSAGE);
        setRetryable(false);
    }

    public CategoryNotFoundException(String categoryName) {
        super(DEFAULT_STATUS, CODE, "Category '" + categoryName + "' does not exist.");
        setRetryable(false);
    }
}
