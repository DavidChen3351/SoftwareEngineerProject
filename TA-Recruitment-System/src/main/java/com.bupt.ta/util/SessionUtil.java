package com.bupt.ta.util;

import com.bupt.ta.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Session-related helpers for setting/getting current user and managing session lifecycle.
 */
public class SessionUtil {
    private static final String USER_SESSION_KEY = "currentUser";

    /** Return the current HttpSession, creating one if necessary. */
    public static HttpSession getSession(HttpServletRequest request) {
        return request.getSession();
    }

    /** Store the given user into session and set a 30-minute timeout. */
    public static void setUserToSession(HttpServletRequest request, User user) {
        HttpSession session = getSession(request);
        session.setAttribute(USER_SESSION_KEY, user);
        // session timeout: 30 minutes
        session.setMaxInactiveInterval(30 * 60);
    }

    /** Retrieve the current logged-in user from session, or null if absent. */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = getSession(request);
        return (User) session.getAttribute(USER_SESSION_KEY);
    }

    /** Invalidate the current session (logout). */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = getSession(request);
        session.invalidate();
    }

    /** Return true when a user is currently stored in session. */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }
}