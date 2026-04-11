package com.bupt.ta.util;

import com.buptta.model.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {
    private static final String USER_SESSION_KEY = "currentUser";

    // 获取当前Session
    public static HttpSession getSession(HttpServletRequest request) {
        return request.getSession();
    }

    // 将用户信息存入Session（登录时调用）
    public static void setUserToSession(HttpServletRequest request, User user) {
        HttpSession session = getSession(request);
        session.setAttribute(USER_SESSION_KEY, user);
        // 设置Session超时时间（30分钟）
        session.setMaxInactiveInterval(30 * 60);
    }

    // 从Session获取当前登录用户
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = getSession(request);
        return (User) session.getAttribute(USER_SESSION_KEY);
    }

    // 清除Session（登出时调用）
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = getSession(request);
        session.invalidate();
    }

    // 判断用户是否已登录
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }
}