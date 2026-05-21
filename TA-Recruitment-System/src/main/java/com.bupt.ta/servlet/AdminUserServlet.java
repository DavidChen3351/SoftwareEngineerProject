package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.util.AuthUtil;
import com.bupt.ta.util.DataStore;
import com.bupt.ta.util.PasswordUtil;

import com.bupt.ta.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotatiopackage com.bupt.ta.servlet;

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
n.WebServlet;
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

        String action = resolveAction(request);
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
            redirectDashboard(request, response, "error=User+not+found");
            return;
        }
        if (admin.getId().equals(target.getId()) && "delete".equals(action)) {
            redirectDashboard(request, response, "error=You+cannot+delete+your+own+account");
            return;
        }
        if ("delete".equals(action)) {
            if ("TEACHER".equals(target.getRole())
                    && DataStore.teacherHasActiveRecruitment(getServletContext(), target.getId())) {
                redirectDashboard(request, response, "error=Teacher+has+active+recruitment");
                return;
            }
            if ("TA".equals(target.getRole())) {
                DataStore.removeApplicationsForTaUser(getServletContext(), target);
            }
            boolean removed = users.removeIf(user -> userId.equals(user.getId()));
            if (!removed) {
                redirectDashboard(request, response, "error=User+not+found");
                return;
            }
            DataStore.saveUsers(getServletContext(), users);
            redirectDashboard(request, response, "success=Account+deleted");
            return;
        }
        if ("TEACHER".equals(target.getRole()) && !"TEACHER".equals(role)
                && DataStore.teacherHasActiveRecruitment(getServletContext(), target.getId())) {
            redirectDashboard(request, response, "error=Teacher+has+active+recruitment");
            return;
        }

        target.setRole(role);
        target.setEnabled(enabled);
        if (resetPassword != null && !resetPassword.trim().isEmpty()) {
            if (!ValidationUtil.isStrongPassword(resetPassword.trim())) {
                redirectDashboard(request, response, "error=Password+must+be+at+least+8+characters+and+include+uppercase+lowercase+and+number");
                return;
            }
            target.setPasswordHash(PasswordUtil.hash(resetPassword.trim()));
        }
        DataStore.saveUsers(getServletContext(), users);
        redirectDashboard(request, response, "success=Account+updated");
    }

    private void createTeacher(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            redirectDashboard(request, response, "error=Teacher+name+and+email+are+required");
            return;
        }
        if (DataStore.findUserByEmail(getServletContext(), email.trim()) != null) {
            redirectDashboard(request, response, "error=Email+already+exists");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            redirectDashboard(request, response, "error=Password+is+required");
            return;
        }

        List<User> users = DataStore.loadUsers(getServletContext());
        users.add(new User("T" + ValidationUtil.nowStamp(), "", email.trim(), name.trim(),
                PasswordUtil.hash(password.trim()), "TEACHER", true));
        DataStore.saveUsers(getServletContext(), users);
        redirectDashboard(request, response, "success=Teacher+account+created");
    }

    private static void redirectDashboard(HttpServletRequest request, HttpServletResponse response, String query)
            throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        String url = request.getContextPath() + "/admin/dashboard.jsp?" + query + "&_=" + System.currentTimeMillis();
        response.sendRedirect(url);
    }

    /** Prefer delete when duplicate action params are submitted (hidden update + delete button). */
    private static String resolveAction(HttpServletRequest request) {
        String[] actions = request.getParameterValues("action");
        if (actions == null || actions.length == 0) {
            return null;
        }
        for (String value : actions) {
            if ("delete".equals(value)) {
                return "delete";
            }
        }
        return actions[0];
    }
}
