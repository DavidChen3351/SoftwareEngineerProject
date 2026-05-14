<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicationRecord" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="com.bupt.ta.util.ValidationUtil" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !"TA".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Job> jobs = DataStore.loadJobs(application);
    String sort = request.getParameter("sort");
    if ("deadline".equals(sort)) {
        Collections.sort(jobs, new Comparator<Job>() {
            public int compare(Job left, Job right) {
                return left.getDeadline().compareTo(right.getDeadline());
            }
        });
    } else if ("course".equals(sort)) {
        Collections.sort(jobs, new Comparator<Job>() {
            public int compare(Job left, Job right) {
                return left.getCourseName().compareToIgnoreCase(right.getCourseName());
            }
        });
    }
    List<ApplicationRecord> myApplications = DataStore.findApplicationsByStudent(application, currentUser.getStudentId());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>TA Positions | BUPT TA Recruitment</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/styles.css">
</head>
<body>
<div class="topbar">
    <div>
        <span class="eyebrow">TA Workspace</span>
        <h1>Available Positions</h1>
    </div>
    <div class="nav-actions">
        <span><%=currentUser.getName()%> (<%=currentUser.getStudentId()%>)</span>
        <a href="<%=request.getContextPath()%>/auth/logout">Logout</a>
    </div>
</div>
<main class="page-shell jobs-page">
    <% if (request.getParameter("error") != null) { %>
    <div class="alert error"><%=request.getParameter("error")%></div>
    <% } %>
    <% if (request.getParameter("success") != null) { %>
    <div class="alert success"><%=request.getParameter("success")%></div>
    <% } %>
    <% if (!myApplications.isEmpty()) { %>
    <div class="table-card ta-my-apps">
        <h3>My applications</h3>
        <p class="subtle" style="margin-top: 0;">Review status for positions you have applied to.</p>
        <table>
            <thead>
            <tr>
                <th>Position</th>
                <th>Course</th>
                <th>Submitted</th>
                <th>Result</th>
            </tr>
            </thead>
            <tbody>
            <% for (ApplicationRecord row : myApplications) {
                Job linked = DataStore.findJob(application, row.getJobId());
                String jobTitle = linked != null ? linked.getTitle() : "(Position removed)";
                String courseName = linked != null ? linked.getCourseName() : "—";
                String st = row.getStatus() == null ? "PENDING" : row.getStatus();
                String label = "ACCEPTED".equals(st) ? "Accepted" : ("REJECTED".equals(st) ? "Rejected" : "Pending");
            %>
            <tr>
                <td><strong><%=jobTitle%></strong></td>
                <td><%=courseName%></td>
                <td><%=row.getSubmittedAt()%></td>
                <td><span class="status <%=st.toLowerCase()%>"><%=label%></span></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
    <div class="toolbar">
        <a class="<%="deadline".equals(sort) ? "chip active" : "chip"%>" href="?sort=deadline">Sort by deadline</a>
        <a class="<%="course".equals(sort) ? "chip active" : "chip"%>" href="?sort=course">Sort by course</a>
        <a class="<%=(sort == null || sort.isEmpty()) ? "chip active" : "chip"%>" href="jobs.jsp">Default order</a>
    </div>
    <div class="table-card">
        <table>
            <thead>
            <tr>
                <th>Position</th>
                <th>Course</th>
                <th>Workload</th>
                <th>Deadline</th>
                <th>Remaining Slots</th>
                <th>Action / Status</th>
            </tr>
            </thead>
            <tbody>
            <% for (Job job : jobs) {
                boolean available = ValidationUtil.isJobOpenForApplications(job);
                boolean applied = DataStore.hasApplied(application, job.getId(), currentUser.getStudentId());
                ApplicationRecord myApp = applied ? DataStore.findApplicationByJobAndStudent(application, job.getId(), currentUser.getStudentId()) : null;
                String appStatus = myApp != null && myApp.getStatus() != null ? myApp.getStatus() : "PENDING";
            %>
            <tr>
                <td><strong><%=job.getTitle()%></strong><span class="subtle">Posted by <%=job.getTeacherName()%></span></td>
                <td><%=job.getCourseName()%></td>
                <td><%=job.getWorkload()%></td>
                <td><%=job.getDeadline().replace("T", " ")%></td>
                <td><%=job.getRemainingSlots()%> / <%=job.getTotalSlots()%></td>
                <td>
                    <% if (applied) {
                        String statusLabel = "ACCEPTED".equals(appStatus) ? "Accepted" : ("REJECTED".equals(appStatus) ? "Rejected" : "Pending");
                    %>
                    <div class="ta-status-cell">
                        <span class="status <%=appStatus.toLowerCase()%>"><%=statusLabel%></span>
                        <span class="subtle">Applied</span>
                    </div>
                    <% } else if (available) { %>
                    <a class="primary-btn small" href="<%=request.getContextPath()%>/ta/apply.jsp?jobId=<%=job.getId()%>">Apply</a>
                    <% } else { %>
                    <button class="disabled-btn" disabled>Closed</button>
                    <% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
