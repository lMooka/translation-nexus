package dev.mooka.translationnexus.security;

public final class Roles {
    private Roles() {}

    public static final String TRANSLATOR = "TRANSLATOR";
    public static final String REVIEWER = "REVIEWER";
    public static final String MANAGER = "MANAGER";
    public static final String ADMIN = "ADMIN";

    public static final String ROLE_PREFIX = "ROLE_";
    
    public static final String ROLE_TRANSLATOR = ROLE_PREFIX + TRANSLATOR;
    public static final String ROLE_REVIEWER = ROLE_PREFIX + REVIEWER;
    public static final String ROLE_MANAGER = ROLE_PREFIX + MANAGER;
    public static final String ROLE_ADMIN = ROLE_PREFIX + ADMIN;
}
