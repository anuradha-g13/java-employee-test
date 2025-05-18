package com.reliaquest.api.util;

import java.util.UUID;

public class ValidationUtil {

    public static final String STRING_REGEX = "^[A-Za-z\\s'-]{1,50}$";

    public static boolean validateString(String value){
        if (value == null) {
            return false;
        }
        return value.matches(STRING_REGEX);
    }

    public static boolean isValidUUID(String uuidStr) {
        if (uuidStr == null) {
            return false;
        }
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
