package ua.com.solidity.web.security.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Role {
    ADVANCED("Risk Stand Advanced"),
    BASIC("Risk Stand Basic");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {return this.role;}

    public static List<String> getAcceptedRoles() {
       return  Arrays.stream(Role.values()).map(role ->  role.getRole()).collect(Collectors.toList());
    }


}
