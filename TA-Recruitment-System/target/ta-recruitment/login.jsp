<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login | BUPT TA Recruitment</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/styles.css">
</head>
<body class="auth-page">
<div class="auth-shell">
    <div class="brand-panel">
        <span class="eyebrow">BUPT International College</span>
        <h1>TA Recruitment System</h1>
        <p>Role-based recruitment workflow for teaching assistants, instructors, and administrators.</p>
        <div class="role-cards">
            <div><strong>TA</strong><span>Browse positions and submit applications</span></div>
            <div><strong>Teacher</strong><span>Publish openings and review candidates</span></div>
            <div><strong>Admin</strong><span>Control accounts and permissions</span></div>
        </div>
    </div>
    <div class="form-panel">
        <h2>Sign in</h2>
        <% if (request.getParameter("error") != null) { %>
        <div class="alert error"><%=request.getParameter("error")%></div>
        <% } %>
        <% if (request.getParameter("success") != null) { %>
        <div class="alert success"><%=request.getParameter("success")%></div>
        <% } %>
        <form action="<%=request.getContextPath()%>/auth/login" method="post" class="stack-form">
            <label>Email
                <input type="email" name="email" required>
            </label>
            <label>Password
                <input type="password" name="password" required>
            </label>
            <button type="submit" class="primary-btn">Login</button>
        </form>
        <p class="helper-text">TA accounts can self-register. Teacher and admin accounts are managed by the system.</p>
        <a href="<%=request.getContextPath()%>/register.jsp" class="secondary-link">Create TA account</a>
        <div class="demo-box">
            <p>Demo accounts</p>
            <span>TA: ta1@bupt.edu.cn / 123456</span>
            <span>Teacher: teacher1@bupt.edu.cn / 123456</span>
            <span>Admin: admin@bupt.edu.cn / 123456</span>
        </div>
    </div>
</div>
</body>
</html>
