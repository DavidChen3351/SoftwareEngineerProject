package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.util.DataStore;
import com.bupt.ta.util.PasswordUtil;
import com.bupt.ta.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String role = request.getParameter("role");
        String studentId = request.getParameter("studentId");
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!"TA".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Only+TA+registration+is+open");
            return;
        }
        if (isBlank(studentId) || isBlank(email) || isBlank(name) || isBlank(password)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=All+fields+are+required");
            return;
        }
        if (!isUniversityEmail(email)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Please+use+your+BUPT+university+email");
            return;
        }
        if (!password.equals(confirmPassword)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Passwords+do+not+match");
            return;
        }
        if (!isStrongPassword(password)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Password+must+be+at+least+8+characters+and+include+uppercase+lowercase+and+number");
            return;
        }
        if (DataStore.findUserByEmail(getServletContext(), email) != null) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Email+already+exists");
            return;
        }
        if (DataStore.findUserByStudentId(getServletContext(), studentId) != null) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Student+ID+already+exists");
            return;
        }
        List<User> users = DataStore.loadUsers(getServletContext());
        users.add(new User("U" + ValidationUtil.nowStamp(), studentId, email, name,
                PasswordUtil.hash(password), "TA", true));
        DataStore.saveUsers(getServletContext(), users);
        response.sendRedirect(request.getContextPath() + "/login.jsp?success=Registration+completed");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isUniversityEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        return normalized.endsWith("@bupt.edu.cn") || normalized.endsWith("@mail.bupt.edu.cn");
    }

    private static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (int index = 0; index < password.length(); index++) {
            char ch = password.charAt(index);
            hasUpper = hasUpper || Character.isUpperCase(ch);
            hasLower = hasLower || Character.isLowerCase(ch);
            hasDigit = hasDigit || Character.isDigit(ch);
        }
        return hasUpper && hasLower && hasDigit;
    }
}
