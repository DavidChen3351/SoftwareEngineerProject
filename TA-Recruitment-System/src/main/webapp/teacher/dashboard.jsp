<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicationRecord" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="com.bupt.ta.util.ValidationUtil" %>
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
                <label>Module Code (optional)
                    <input type="text" name="moduleCode" placeholder="e.g. CS201">
                </label>
                <label>Module Name
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
            <p class="subtle" style="margin-top: 0;">
                <strong>Pause applications</strong> — position stays on the TA list, but no new applications.
                <strong>Cancel position</strong> — withdraws the post and removes it from the TA portal (existing applications remain for your review).
            </p>
            <table>
                <thead>
                <tr>
                    <th>Position</th>
                    <th>Module Code</th>
                    <th>Module Name</th>
                    <th>Quota</th>
                    <th>Deadline</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <% for (Job job : jobs) {
                    String statusLabel;
                    String statusClass;
                    if (job.isCancelled()) {
                        statusLabel = "Cancelled";
                        statusClass = "position-status cancelled";
                    } else if (ValidationUtil.isJobOpenForApplications(job)) {
                        statusLabel = "Open";
                        statusClass = "position-status available";
                    } else if (!job.isAcceptingApplications()) {
                        statusLabel = "Paused";
                        statusClass = "position-status paused";
                    } else if (!ValidationUtil.isActiveDeadline(job.getDeadline())) {
                        statusLabel = "Closed";
                        statusClass = "position-status closed";
                    } else if (job.getRemainingSlots() <= 0) {
                        statusLabel = "Full";
                        statusClass = "position-status no-vacancy";
                    } else {
                        statusLabel = "Closed";
                        statusClass = "position-status closed";
                    }
                %>
                <tr class="<%=job.isCancelled() ? "teacher-job-row-cancelled" : ""%>">
                    <td><%=job.getTitle()%></td>
                    <td><%=job.getModuleCode() == null || job.getModuleCode().isEmpty() ? "-" : job.getModuleCode()%></td>
                    <td><%=job.getCourseName()%></td>
                    <td><%=job.getFilledSlots()%> / <%=job.getTotalSlots()%></td>
                    <td><%=job.getDeadline().replace("T", " ")%></td>
                    <td><span class="<%=statusClass%>"><%=statusLabel%></span></td>
                    <td>
                        <div class="teacher-job-manage">
                            <% if (!job.isCancelled()) { %>
                            <form class="inline-form" method="post" action="<%=request.getContextPath()%>/teacher/jobs/listing">
                                <input type="hidden" name="jobId" value="<%=job.getId()%>">
                                <% if (job.isAcceptingApplications()) { %>
                                <input type="hidden" name="accepting" value="false">
                                <button type="submit" class="secondary-btn small" title="Stop new applications; position still visible to TAs">Pause applications</button>
                                <% } else { %>
                                <input type="hidden" name="accepting" value="true">
                                <button type="submit" class="primary-btn small" title="Allow TAs to apply again">Resume applications</button>
                                <% } %>
                            </form>
                            <form class="inline-form teacher-quota-form" method="post" action="<%=request.getContextPath()%>/teacher/jobs/quota">
                                <input type="hidden" name="jobId" value="<%=job.getId()%>">
                                <span class="subtle">Total slots</span>
                                <input type="number" name="totalSlots" min="<%=job.getFilledSlots()%>" value="<%=job.getTotalSlots()%>" class="quota-input" title="Cannot be less than already filled">
                                <button type="submit" class="secondary-btn small">Update quota</button>
                            </form>
                            <form class="inline-form" method="post" action="<%=request.getContextPath()%>/teacher/jobs/cancel"
                                  onsubmit="return confirm('Cancel this position? It will be removed from the TA job list. Existing applications stay in your review table.');">
                                <input type="hidden" name="jobId" value="<%=job.getId()%>">
                                <button type="submit" class="danger-btn small">Cancel position</button>
                            </form>
                            <% } else { %>
                            <span class="subtle">Withdrawn from TA portal</span>
                            <% } %>
                            <form class="inline-form" method="post" action="<%=request.getContextPath()%>/teacher/jobs/delete"
                                  onsubmit="return confirm('Permanently delete this position and all related applications? This cannot be undone.');">
                                <input type="hidden" name="jobId" value="<%=job.getId()%>">
                                <button type="submit" class="danger-btn small">Delete permanently</button>
                            </form>
                        </div>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
    <section class="table-card">
        <h3>Applications for my courses</h3>
        <% if (applications.isEmpty()) { %>
        <p class="empty-state-message">No applications yet. Submitted TA applications for your positions will appear here.</p>
        <% } else { %>
        <table>
            <thead>
            <tr>
                <th>Student</th>
                <th>Student ID</th>
                <th>Job</th>
                <th>Module</th>
                <th>Skills</th>
                <th>Submitted</th>
                <th>Reviewed</th>
                <th>Status</th>
                <th>Resume</th>
                <th>Decision</th>
            </tr>
            </thead>
            <tbody>
            <% for (ApplicationRecord item : applications) {
                String title = "";
                String module = "";
                for (Job job : jobs) {
                    if (job.getId().equals(item.getJobId())) {
                        title = job.getTitle();
                        String moduleCode = job.getModuleCode() == null || job.getModuleCode().isEmpty() ? "-" : job.getModuleCode();
                        module = moduleCode + " / " + job.getCourseName();
                        break;
                    }
                }
            %>
            <tr>
                <td><strong><%=item.getStudentName()%></strong><span class="subtle"><%=item.getStudentEmail()%></span></td>
                <td><%=item.getStudentId()%></td>
                <td><%=title%></td>
                <td><%=module%></td>
                <td><%=item.getSkills()%></td>
                <td><%=item.getSubmittedAt()%></td>
                <td><%="ACCEPTED".equals(item.getStatus()) && item.getReviewedAt() != null && !item.getReviewedAt().isEmpty() ? item.getReviewedAt() : "-"%></td>
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
        <% } %>
    </section>
</main>
</body>
</html>
