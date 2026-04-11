<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Register | BUPT TA Recruitment</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/styles.css">
</head>
<body class="auth-page">
<div class="auth-shell">
    <div class="brand-panel">
        <span class="eyebrow">BUPT International College</span>
        <h1>Create a TA account</h1>
        <p>Student ID and email must be unique.</p>
    </div>
    <div class="form-panel">
        <h2>TA Registration</h2>
        <% if (request.getParameter("error") != null) { %>
        <div class="alert error"><%=request.getParameter("error")%></div>
        <% } %>
        <form action="<%=request.getContextPath()%>/auth/register" method="post" class="stack-form">
            <input type="hidden" name="role" value="TA">
            <label>Student ID
                <input type="text" name="studentId" required>
            </label>
            <label>Full name
                <input type="text" name="name" required>
            </label>
            <label>Email
                <input type="email" name="email" required>
            </label>
            <label>Password
                <input type="password" name="password" required>
            </label>
            <button type="submit" class="primary-btn">Register</button>
        </form>
        <a href="<%=request.getContextPath()%>/login.jsp" class="secondary-link">Back to login</a>
    </div>
</div>
</body>
</html>
