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
