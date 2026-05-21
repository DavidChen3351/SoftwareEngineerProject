<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String query = request.getParameter("query") == null ? "" : request.getParameter("query").trim().toLowerCase();
    String filterRole = request.getParameter("role") == null ? "" : request.getParameter("role");
    String filterEnabled = request.getParameter("enabled") == null ? "" : request.getParameter("enabled");
    List<User> users = DataStore.loadUsers(application);
    List<User> filteredUsers = new ArrayList<User>();
    for (User user : users) {
        boolean matchesQuery = query.isEmpty() || user.getName().toLowerCase().contains(query)
                || user.getEmail().toLowerCase().contains(query);
        boolean matchesRole = filterRole.isEmpty() || filterRole.equals(user.getRole());
        boolean matchesEnabled = filterEnabled.isEmpty()
                || ("true".equals(filterEnabled) && user.isEnabled())
                || ("false".equals(filterEnabled) && !user.isEnabled());
        if (matchesQuery && matchesRole && matchesEnabled) {
            filteredUsers.add(user);
        }
    }
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
    <section class="detail-grid admin-grid">
        <div class="form-card">
            <h3>Search and filter users</h3>
            <form action="<%=request.getContextPath()%>/admin/dashboard.jsp" method="get" class="stack-form">
                <label>Keyword
                    <input type="text" name="query" placeholder="Search name or email" value="<%=query%>">
                </label>
                <label>Role
                    <select name="role">
                        <option value="" <%=filterRole.isEmpty() ? "selected" : ""%>>All</option>
                        <option value="TA" <%="TA".equals(filterRole) ? "selected" : ""%>>TA</option>
                        <option value="TEACHER" <%="TEACHER".equals(filterRole) ? "selected" : ""%>>Teacher</option>
                        <option value="ADMIN" <%="ADMIN".equals(filterRole) ? "selected" : ""%>>Admin</option>
                    </select>
                </label>
                <label>Status
                    <select name="enabled">
                        <option value="" <%=filterEnabled.isEmpty() ? "selected" : ""%>>All</option>
                        <option value="true" <%="true".equals(filterEnabled) ? "selected" : ""%>>Enabled</option>
                        <option value="false" <%="false".equals(filterEnabled) ? "selected" : ""%>>Disabled</option>
                    </select>
                </label>
                <div class="admin-actions">
                    <button type="submit" class="primary-btn small">Search</button>
                    <a href="<%=request.getContextPath()%>/admin/dashboard.jsp" class="secondary-btn small">Clear</a>
                </div>
            </form>
        </div>
        <div class="info-card">
            <h3>Batch actions</h3>
            <form id="batchForm" action="<%=request.getContextPath()%>/admin/users/update" method="post" class="stack-form">
                <button type="submit" name="action" value="batchEnable" class="primary-btn">Enable selected</button>
                <button type="submit" name="action" value="batchDisable" class="danger-btn">Disable selected</button>
                <p class="field-hint">Select multiple users and then choose a batch action.</p>
            </form>
        </div>
    </section>
    <div class="table-card">
        <table>
            <thead>
            <tr>
                <th><input type="checkbox" onclick="toggleSelectAll(this)" title="Select all"></th>
                <th>Name</th>
                <th>Email</th>
                <th>Student ID</th>
                <th>Role</th>
                <th>Status</th>
                <th>Manage</th>
            </tr>
            </thead>
            <tbody>
            <% for (User user : filteredUsers) {
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
                <td>
                    <input type="checkbox" name="selectedUserIds" value="<%=user.getId()%>" form="batchForm" <%=isSelf ? "disabled" : ""%>>
                </td>
                <td><strong><%=user.getName()%></strong></td>
                <td><%=user.getEmail()%></td>
                <td><%=user.getStudentId() == null || user.getStudentId().isEmpty() ? "-" : user.getStudentId()%></td>
                <td><%=user.getRole()%></td>
                <td><%=user.isEnabled() ? "Enabled" : "Disabled"%></td>
                <td>
                    <form action="<%=request.getContextPath()%>/admin/users/update" method="post" class="admin-form">
                        <input type="hidden" name="userId" value="<%=user.getId()%>">
                        <input type="hidden" name="action" value="update">
                        <% if (isSelf) { %>
                        <input type="hidden" name="role" value="<%=user.getRole()%>">
                        <input type="hidden" name="enabled" value="<%=user.isEnabled() ? "true" : "false"%>">
                        <% } %>
                        <select name="role" <%=isSelf ? "disabled" : ""%>>
                            <option value="TA" <%="TA".equals(user.getRole()) ? "selected" : ""%>>TA</option>
                            <option value="TEACHER" <%="TEACHER".equals(user.getRole()) ? "selected" : ""%>>Teacher</option>
                            <option value="ADMIN" <%="ADMIN".equals(user.getRole()) ? "selected" : ""%>>Admin</option>
                        </select>
                        <select name="enabled" <%=isSelf ? "disabled" : ""%>>
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
                        <% if (isSelf) { %>
                        <span class="field-hint warning-text">Your own admin role and status cannot be changed here.</span>
                        <% } else if (!deleteReason.isEmpty()) { %>
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
<script>
    function toggleSelectAll(source) {
        const checkboxes = document.querySelectorAll('input[name="selectedUserIds"]');
        checkboxes.forEach(function (checkbox) {
            if (!checkbox.disabled) {
                checkbox.checked = source.checked;
            }
        });
    }
</script>
</body>
</html>