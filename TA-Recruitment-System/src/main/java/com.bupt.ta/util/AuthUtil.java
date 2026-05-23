package com.bupt.ta.util;

import com.bupt.ta.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Authentication helper utilities.
 * Provides helpers to fetch the current user from session and check role membership.
 */
public final class AuthUtil {
    private AuthUtil() {
    }

    /**
     * Return the current logged-in User stored in the HTTP session.
     * Returns null when no session or no user present.
     */
    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }

    /**
     * Check whether the current user has the given role.
     * Returns false when no user is logged in.
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        User user = currentUser(request);
        return user != null && role.equals(user.getRole());
    }
}
