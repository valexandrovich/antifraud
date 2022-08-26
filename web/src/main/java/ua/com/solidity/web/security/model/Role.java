package ua.com.solidity.web.security.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Role {
    ADVANCED("Risk Stand Advanced"),
    BASIC("Risk Stand Basic");

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {return this.name;}

    public static List<String> getAcceptedRoles() {
       return  Arrays.stream(Role.values()).map(Role::getName).collect(Collectors.toList());
    }


}
