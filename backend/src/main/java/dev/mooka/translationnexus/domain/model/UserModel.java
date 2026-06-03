package dev.mooka.translationnexus.domain.model;

import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.DuplicateRolesException;
import dev.mooka.translationnexus.exception.impl.DuplicateAllowedLocalesException;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

@Getter
@Setter
public class UserModel {
    private String id;
    private String username;
    private String passwordHash;
    private List<String> roles = new ArrayList<>();
    private List<String> allowedLocales = new ArrayList<>();

    public UserModel() {
    }

    @Builder
    public UserModel(String id, String username, String passwordHash, List<String> roles, List<String> allowedLocales)
            throws BusinessException {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        setRoles(roles != null ? roles : new ArrayList<>());
        setAllowedLocales(allowedLocales != null ? allowedLocales : new ArrayList<>());
    }

    public void setRoles(List<String> roles) throws BusinessException {
        // check for duplicates
        if (roles != null && roles.size() != new HashSet<>(roles).size()) {
            throw new DuplicateRolesException();
        }
        this.roles = roles;
    }

    public void setAllowedLocales(List<String> allowedLocales) throws BusinessException {
        // check for duplicates
        if (allowedLocales != null && allowedLocales.size() != new HashSet<>(allowedLocales).size()) {
            throw new DuplicateAllowedLocalesException();
        }
        this.allowedLocales = allowedLocales;
    }
}
