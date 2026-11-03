package com.kdt.cafe.utils.validate;


public class EmailValidator {
    private static final String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$";

    public static boolean isValid(String email) {
        return email.matches(EMAIL_REGEX);
    }
}
