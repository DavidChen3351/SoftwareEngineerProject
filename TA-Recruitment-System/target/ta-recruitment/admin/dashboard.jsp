<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<User> users = DataStore.loadUsers(application);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Console | BUPT TA Recruitment</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/styles.css">
</head>
<body>
<div class="topbar">
    <div>
        <span class="eyebrow">Administrator</span>
        <h1>Account Control Center</h1>
    </div>
    <div class="nav-actions">
        <span><%=currentUser.getName()%></span>
        <a href="<%=request.getContextPath()%>/auth/logout">Logout</a>
    </div>
</div>
<main class="page-shell">
    <% if (request.getParameter("error") != null) { %>
    <div class="alert error"><%=request.getParameter("error")%></div>
    <% } %>
    <% if (request.getParameter("success") != null) { %>
    <div class="alert success"><%=request.getParameter("success")%></div>
    <% } %>
    <section class="detail-grid admin-grid">
        <div class="form-card">
            <h3>Create teacher account</h3>
            <form action="<%=request.getContextPath()%>/admin/users/update" method="post" class="stack-form">
                <input type="hidden" name="action" value="createTeacher">
                <label>Teacher name
                    <input type="text" name="name" required>
                </label>
                <label>Email
                    <input type="email" name="email" required>
                </label>
                <label>Initial password
                    <input type="password" name="password" required>
                </label>
                <button type="submit" class="primary-btn">Create teacher</button>
            </form>
        </div>
        <div class="info-card">
            <h3>Permission rules</h3>
            <p>Teachers with active recruitment cannot be reassigned or deleted. Accounts with protected actions show inline warnings before submission.</p>
        </div>
    </section>
    <div class="table-card">
        <table>
            <thead>
            <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Student ID</th>
                <th>Role</th>
                <th>Status</th>
                <th>Manage</th>
            </tr>
            </thead>
            <tbody>
            <% for (User user : users) {
                boolean isSelf = currentUser.getId().equals(user.getId());
                boolean hasActiveRecruitment = "TEACHER".equals(user.getRole()) && DataStore.teacherHasActiveRecruitment(application, user.getId());
                boolean deleteBlocked = isSelf || hasActiveRecruitment;
                String deleteReason = "";
                if (isSelf) {
                    deleteReason = "Current admin account cannot be deleted.";
                } else if (hasActiveRecruitment) {
                    deleteReason = "Delete disabled: this teacher still has active recruitment.";
                }
            %>
            <tr>
                <td><strong><%=user.getName()%></strong></td>
                <td><%=user.getEmail()%></td>
                <td><%=user.getStudentId() == null || user.getStudentId().isEmpty() ? "-" : user.getStudentId()%></td>
                <td><%=user.getRole()%></td>
                <td><%=user.isEnabled() ? "Enabled" : "Disabled"%></td>
                <td>
                    <form action="<%=request.getContextPath()%>/admin/users/update" method="post" class="admin-form">
                        <input type="hidden" name="userId" value="<%=user.getId()%>">
                        <input type="hidden" name="action" value="update">
                        <select name="role">
                            <option value="TA" <%="TA".equals(user.getRole()) ? "selected" : ""%>>TA</option>
                            <option value="TEACHER" <%="TEACHER".equals(user.getRole()) ? "selected" : ""%>>Teacher</option>
                            <option value="ADMIN" <%="ADMIN".equals(user.getRole()) ? "selected" : ""%>>Admin</option>
                        </select>
                        <select name="enabled">
                            <option value="true" <%=user.isEnabled() ? "selected" : ""%>>Enabled</option>
                            <option value="false" <%=!user.isEnabled() ? "selected" : ""%>>Disabled</option>
                        </select>
                        <input type="text" name="resetPassword" placeholder="New password (optional)">
                        <div class="admin-actions">
                            <button type="submit" class="primary-btn small">Save</button>
                            <% if (deleteBlocked) { %>
                            <button type="button" class="disabled-btn small" disabled>Delete</button>
                            <% } else { %>
                            <button type="submit" name="action" value="delete" class="danger-btn small"
                                    onclick="return confirm('Delete this account?');">Delete</button>
                            <% } %>
                        </div>
                        <% if (!deleteReason.isEmpty()) { %>
                        <span class="field-hint warning-text"><%=deleteReason%></span>
                        <% } %>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
