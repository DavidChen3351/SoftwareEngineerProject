package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.util.DataStore;
import com.bupt.ta.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = DataStore.findUserByEmail(getServletContext(), email);
        if (user == null || !user.isEnabled() || !user.getPasswordHash().equals(PasswordUtil.hash(password))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=Invalid+credentials+or+disabled+account");
            return;
        }

        request.getSession(true).setAttribute("currentUser", user);
        if ("TA".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp");
            return;
        }
        if ("TEACHER".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
    }
}
