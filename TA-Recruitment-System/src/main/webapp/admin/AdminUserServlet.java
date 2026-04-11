package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.util.AuthUtil;
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

@WebServlet("/admin/users/update")
public class AdminUserServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User admin = AuthUtil.currentUser(request);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if ("createTeacher".equals(action)) {
            createTeacher(request, response);
            return;
        }

        String userId = request.getParameter("userId");
        String role = request.getParameter("role");
        boolean enabled = "true".equals(request.getParameter("enabled"));
        String resetPassword = request.getParameter("resetPassword");

        List<User> users = DataStore.loadUsers(getServletContext());
        User target = null;
        for (User user : users) {
            if (user.getId().equals(userId)) {
                target = user;
                break;
            }
        }
        if (target == null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=User+not+found");
            return;
        }
        if (admin.getId().equals(target.getId()) && "delete".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=You+cannot+delete+your+own+account");
            return;
        }
        if ("delete".equals(action)) {
            if ("TEACHER".equals(target.getRole())
                    && DataStore.teacherHasActiveRecruitment(getServletContext(), target.getId())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=Teacher+has+active+recruitment");
                return;
            }
            users.remove(target);
            DataStore.saveUsers(getServletContext(), users);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?success=Account+deleted");
            return;
        }
        if ("TEACHER".equals(target.getRole()) && !"TEACHER".equals(role)
                && DataStore.teacherHasActiveRecruitment(getServletContext(), target.getId())) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=Teacher+has+active+recruitment");
            return;
        }

        target.setRole(role);
        target.setEnabled(enabled);
        if (resetPassword != null && !resetPassword.trim().isEmpty()) {
            target.setPasswordHash(PasswordUtil.hash(resetPassword.trim()));
        }
        DataStore.saveUsers(getServletContext(), users);
        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?success=Account+updated");
    }

    private void createTeacher(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=Teacher+name+and+email+are+required");
            return;
        }
        if (DataStore.findUserByEmail(getServletContext(), email.trim()) != null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=Email+already+exists");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?error=Password+is+required");
            return;
        }

        List<User> users = DataStore.loadUsers(getServletContext());
        users.add(new User("T" + ValidationUtil.nowStamp(), "", email.trim(), name.trim(),
                PasswordUtil.hash(password.trim()), "TEACHER", true));
        DataStore.saveUsers(getServletContext(), users);
        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp?success=Teacher+account+created");
    }
}
