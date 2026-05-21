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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/admin/users/update")
public class AdminUserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User admin = AuthUtil.currentUser(request);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User admin = AuthUtil.currentUser(request);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = safeParam(request.getParameter("action"), "update");
        if ("createTeacher".equals(action)) {
            createTeacher(request, response);
            return;
        }

        if ("batchEnable".equals(action) || "batchDisable".equals(action)) {
            String[] selectedUserIds = request.getParameterValues("selectedUserIds");
            if (selectedUserIds == null || selectedUserIds.length == 0) {
                redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "No+users+selected");
                return;
            }
            boolean enabledValue = "batchEnable".equals(action);
            Set<String> selectedIds = new HashSet<String>(Arrays.asList(selectedUserIds));
            List<User> users = DataStore.loadUsers(getServletContext());
            int updatedCount = 0;
            for (User user : users) {
                if (!selectedIds.contains(user.getId())) {
                    continue;
                }
                if (!enabledValue && admin.getId().equals(user.getId())) {
                    redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Cannot+disable+your+own+account");
                    return;
                }
                if (!enabledValue && "TEACHER".equals(user.getRole())
                        && DataStore.teacherHasActiveRecruitment(getServletContext(), user.getId())) {
                    redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Cannot+disable+teacher+with+active+recruitment");
                    return;
                }
                user.setEnabled(enabledValue);
                updatedCount++;
            }
            if (updatedCount == 0) {
                redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "No+valid+users+selected");
                return;
            }
            DataStore.saveUsers(getServletContext(), users);
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "success", updatedCount + "+accounts+updated");
            return;
        }

        String userId = safeParam(request.getParameter("userId"), "");
        String role = safeParam(request.getParameter("role"), "TA");
        boolean enabled = "true".equals(request.getParameter("enabled"));
        String resetPassword = request.getParameter("resetPassword");

        if (userId.isEmpty()) {
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Missing+user+selection");
            return;
        }
        if (!isValidRole(role)) {
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Invalid+role+value");
            return;
        }

        List<User> users = DataStore.loadUsers(getServletContext());
        User target = null;
        for (User user : users) {
            if (user.getId().equals(userId)) {
                target = user;
                break;
            }
        }
        if (target == null) {
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "User+not+found");
            return;
        }

        boolean isSelf = admin.getId().equals(target.getId());
        if ("delete".equals(action)) {
            if (isSelf) {
                redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Cannot+delete+your+own+account");
                return;
            }
            if ("TEACHER".equals(target.getRole())
                    && DataStore.teacherHasActiveRecruitment(getServletContext(), target.getId())) {
                redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Teacher+has+active+recruitment");
                return;
            }
            users.remove(target);
            DataStore.saveUsers(getServletContext(), users);
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "success", "Account+deleted");
            return;
        }

        if (isSelf && !"ADMIN".equals(role)) {
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Cannot+demote+your+own+account");
            return;
        }
        if (isSelf && !enabled) {
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Cannot+disable+your+own+account");
            return;
        }
        if ("TEACHER".equals(target.getRole()) && !"TEACHER".equals(role)
                && DataStore.teacherHasActiveRecruitment(getServletContext(), target.getId())) {
            redirectWithMessage(request, response, "/admin/dashboard.jsp", "error", "Teacher+has+active+recruitment");
            return;
        }

        target.setRole(role);
        target.setEnabled(enabled);
        if (resetPassword != null && !resetPassword.trim().isEmpty()) {
            target.setPasswordHash(PasswordUtil.hash(resetPassword.trim()));
        }

        DataStore.saveUsers(getServletContext(), users);
        redirectWithMessage(request, response, "/admin/dashboard.jsp", "success", "Account+updated");
    }

    private void createTeacher(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = safeParam(request.getParameter("name"), "").trim();
        String email = safeParam(request.getParameter("email"), "").trim().toLowerCase();
        String password = safeParam(request.getParameter("password"), "").trim();

        if (name.isEmpty() || email.isEmpty()) {
            redirectWithMessage(response, "/admin/dashboard.jsp", "error", "Teacher+name+and+email+are+required");
            return;
        }
        if (DataStore.findUserByEmail(getServletContext(), email) != null) {
            redirectWithMessage(response, "/admin/dashboard.jsp", "error", "Email+already+exists");
            return;
        }
        if (password.isEmpty()) {
            redirectWithMessage(response, "/admin/dashboard.jsp", "error", "Password+is+required");
            return;
        }

        List<User> users = DataStore.loadUsers(getServletContext());
        users.add(new User("T" + ValidationUtil.nowStamp(), "", email, name,
                PasswordUtil.hash(password), "TEACHER", true));
        DataStore.saveUsers(getServletContext(), users);
        redirectWithMessage(request, response, "/admin/dashboard.jsp", "success", "Teacher+account+created");
    }

    private String safeParam(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private boolean isValidRole(String role) {
        return "TA".equals(role) || "TEACHER".equals(role) || "ADMIN".equals(role);
    }

    private void redirectWithMessage(HttpServletRequest request, HttpServletResponse response, String path, String type, String message) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + path + "?" + type + "=" + message));
    }
}
