package com.medwiz.novare_crm.enums;

import lombok.Getter;

@Getter
public enum Role {

    ADMIN("ADMIN"),
    DOCTOR("DOCTOR"),
    MEMBER("MEMBER");
    private final String name;

    Role(String name) {
        this.name = name;
    }
}
