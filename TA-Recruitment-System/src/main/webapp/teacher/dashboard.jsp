<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicationRecord" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !"TEACHER".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Job> allJobs = DataStore.loadJobs(application);
    List<ApplicationRecord> allApplications = DataStore.loadApplications(application);
    List<Job> jobs = new ArrayList<Job>();
    List<ApplicationRecord> applications = new ArrayList<ApplicationRecord>();
    for (Job job : allJobs) {
        if (currentUser.getId().equals(job.getTeacherId())) {
            jobs.add(job);
        }
    }
    for (ApplicationRecord item : allApplications) {
        for (Job job : jobs) {
            if (job.getId().equals(item.getJobId())) {
                applications.add(item);
                break;
            }
        }
    }
    int pendingCount = 0;
    for (ApplicationRecord item : applications) {
        if ("PENDING".equals(item.getStatus())) {
            pendingCount++;
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Teacher Console | BUPT TA Recruitment</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/styles.css">
</head>
<body>
<div class="topbar">
    <div>
        <span class="eyebrow">Teacher Console</span>
        <h1>Course Recruitment Dashboard</h1>
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
    <section class="stats-row">
        <div class="stat-card"><span>Total applications</span><strong><%=applications.size()%></strong></div>
        <div class="stat-card"><span>Pending review</span><strong><%=pendingCount%></strong></div>
        <div class="stat-card"><span>My positions</span><strong><%=jobs.size()%></strong></div>
    </section>
    <section class="detail-grid">
        <div class="form-card">
            <h3>Publish a new position</h3>
            <form action="<%=request.getContextPath()%>/teacher/jobs/create" method="post" class="stack-form">
                <label>Position title
                    <input type="text" name="title" required>
                </label>
                <label>Course name
                    <input type="text" name="courseName" required>
                </label>
                <label>Workload
                    <input type="text" name="workload" placeholder="e.g. 6 hours/week" required>
                </label>
                <label>Quota
                    <input type="number" name="totalSlots" min="1" required>
                </label>
                <label>Deadline
                    <input type="datetime-local" name="deadline" required>
                </label>
                <button type="submit" class="primary-btn">Publish</button>
            </form>
        </div>
        <div class="table-card">
            <h3>My positions</h3>
            <table>
                <thead>
                <tr>
                    <th>Position</th>
                    <th>Course</th>
                    <th>Quota</th>
                    <th>Deadline</th>
                </tr>
                </thead>
                <tbody>
                <% for (Job job : jobs) { %>
                <tr>
                    <td><%=job.getTitle()%></td>
                    <td><%=job.getCourseName()%></td>
                    <td><%=job.getFilledSlots()%> / <%=job.getTotalSlots()%></td>
                    <td><%=job.getDeadline().replace("T", " ")%></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
    <section class="table-card">
        <h3>Applications for my courses</h3>
        <table>
            <thead>
            <tr>
                <th>Student</th>
                <th>Job</th>
                <th>Skills</th>
                <th>Submitted</th>
                <th>Status</th>
                <th>Resume</th>
                <th>Decision</th>
            </tr>
            </thead>
            <tbody>
            <% for (ApplicationRecord item : applications) {
                String title = "";
                for (Job job : jobs) {
                    if (job.getId().equals(item.getJobId())) {
                        title = job.getTitle();
                        break;
                    }
                }
            %>
            <tr>
                <td><strong><%=item.getStudentName()%></strong><span class="subtle"><%=item.getStudentEmail()%></span></td>
                <td><%=title%></td>
                <td><%=item.getSkills()%></td>
                <td><%=item.getSubmittedAt()%></td>
                <td><span class="status <%=item.getStatus().toLowerCase()%>"><%=item.getStatus()%></span></td>
                <td><a href="<%=request.getContextPath()%>/<%=item.getResumePath()%>" target="_blank">View resume</a></td>
                <td>
                    <form action="<%=request.getContextPath()%>/teacher/applications/update" method="post" class="inline-form">
                        <input type="hidden" name="applicationId" value="<%=item.getId()%>">
                        <button class="accept-btn" type="submit" name="status" value="ACCEPTED">Accept</button>
                        <button class="reject-btn" type="submit" name="status" value="REJECTED">Reject</button>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>
