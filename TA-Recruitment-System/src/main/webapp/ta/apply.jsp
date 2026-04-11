<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="com.bupt.ta.util.ValidationUtil" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !"TA".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    Job job = DataStore.findJob(application, request.getParameter("jobId"));
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp?error=Position+not+found");
        return;
    }
    if (!ValidationUtil.isActiveDeadline(job.getDeadline()) || job.getRemainingSlots() <= 0) {
        response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp?error=This+position+is+closed");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Apply | BUPT TA Recruitment</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/styles.css">
</head>
<body>
<div class="topbar">
    <div>
        <span class="eyebrow">TA Application</span>
        <h1><%=job.getTitle()%></h1>
    </div>
    <div class="nav-actions">
        <a href="<%=request.getContextPath()%>/ta/jobs.jsp">Back to list</a>
        <a href="<%=request.getContextPath()%>/auth/logout">Logout</a>
    </div>
</div>
<main class="page-shell">
    <% if (request.getParameter("error") != null) { %>
    <div class="alert error"><%=request.getParameter("error")%></div>
    <% } %>
    <div class="detail-grid">
        <div class="info-card">
            <h3>Position summary</h3>
            <p><strong>Course:</strong> <%=job.getCourseName()%></p>
            <p><strong>Workload:</strong> <%=job.getWorkload()%></p>
            <p><strong>Deadline:</strong> <%=job.getDeadline().replace("T", " ")%></p>
            <p><strong>Remaining slots:</strong> <%=job.getRemainingSlots()%></p>
        </div>
        <div class="form-card">
            <form action="<%=request.getContextPath()%>/ta/apply" method="post" enctype="multipart/form-data" class="stack-form">
                <input type="hidden" name="jobId" value="<%=job.getId()%>">
                <label>Skills
                    <textarea name="skills" rows="3" required></textarea>
                </label>
                <label>Weekly availability
                    <textarea name="availability" rows="3" required></textarea>
                </label>
                <label>Relevant experience
                    <textarea name="experience" rows="4" required></textarea>
                </label>
                <label>Resume (PDF/DOCX, max 5MB)
                    <input type="file" name="resume" accept=".pdf,.docx" required>
                </label>
                <button type="submit" class="primary-btn">Submit application</button>
            </form>
        </div>
    </div>
</main>
</body>
</html>
