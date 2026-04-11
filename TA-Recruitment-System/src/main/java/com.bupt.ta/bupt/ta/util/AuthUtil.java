package com.bupt.ta.util;

import com.bupt.ta.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class AuthUtil {
    private AuthUtil() {
    }

    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        User user = currentUser(request);
        return user != null && role.equals(user.getRole());
    }
}
