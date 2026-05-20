<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.ApplicationRecord" %>
<%@ page import="com.bupt.ta.model.Job" %>
<%@ page import="com.bupt.ta.model.User" %>
<%@ page import="com.bupt.ta.util.DataStore" %>
<%@ page import="com.bupt.ta.util.PositionStatus" %>
<%@ page import="com.bupt.ta.util.ValidationUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !"TA".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Job> jobs = new ArrayList<Job>();
    for (Job loaded : DataStore.loadJobs(application)) {
        if (!loaded.isCancelled()) {
            jobs.add(loaded);
        }
    }
    String query = request.getParameter("q");
    boolean searching = query != null && !query.trim().isEmpty();
    if (searching) {
        String needle = query.trim().toLowerCase();
        List<Job> filtered = new ArrayList<Job>();
        for (Job job : jobs) {
            if (job.getTitle().toLowerCase().contains(needle)
                    || job.getModuleCode().toLowerCase().contains(needle)
                    || job.getCourseName().toLowerCase().contains(needle)
                    || job.getTeacherName().toLowerCase().contains(needle)
                    || job.getWorkload().toLowerCase().contains(needle)) {
                filtered.add(job);
            }
        }
        jobs = filtered;
    }
    String sort = request.getParameter("sort");
    String queryParam = searching ? "&q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()) : "";
    if ("deadline".equals(sort)) {
        Collections.sort(jobs, new Comparator<Job>() {
            public int compare(Job left, Job right) {
                return left.getDeadline().compareTo(right.getDeadline());
            }
        });
    } else if ("course".equals(sort)) {
        Collections.sort(jobs, new Comparator<Job>() {
            public int compare(Job left, Job right) {
                int byCode = left.getModuleCode().compareToIgnoreCase(right.getModuleCode());
                if (byCode != 0) {
                    return byCode;
                }
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
                <th>Module</th>
                <th>Submitted</th>
                <th>Result</th>
                <th>Position status</th>
            </tr>
            </thead>
            <tbody>
            <% for (ApplicationRecord row : myApplications) {
                Job linked = DataStore.findJob(application, row.getJobId());
                String jobTitle = linked != null ? linked.getTitle() : "(Position removed)";
                String courseName = linked != null ? linked.getCourseName() : "—";
                String moduleCode = linked != null && linked.getModuleCode() != null && !linked.getModuleCode().isEmpty()
                        ? linked.getModuleCode() : "-";
                boolean cancelled = linked != null && linked.isCancelled();
                String st = row.getStatus() == null ? "PENDING" : row.getStatus();
                String label = "ACCEPTED".equals(st) ? "Accepted" : ("REJECTED".equals(st) ? "Rejected" : "Pending");
            %>
            <tr>
                <td><strong><%=jobTitle%></strong></td>
                <td><%=moduleCode%> / <%=courseName%></td>
                <td><%=row.getSubmittedAt()%></td>
                <td><span class="status <%=st.toLowerCase()%>"><%=label%></span></td>
                <td>
                    <% if (cancelled) { %>
                    <span class="position-status cancelled">Position cancelled</span>
                    <% } else { %>
                    <span class="subtle">Active</span>
                    <% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
    <form class="jobs-search-bar" method="get" action="jobs.jsp">
        <% if (sort != null && !sort.isEmpty()) { %>
        <input type="hidden" name="sort" value="<%=sort%>">
        <% } %>
        <input type="search" name="q" value="<%=searching ? query.trim() : ""%>" placeholder="Search by position, module code/name, teacher, or workload" aria-label="Search positions">
        <button type="submit" class="primary-btn small">Search</button>
        <% if (searching) { %>
        <a class="secondary-btn small" href="jobs.jsp<%=(sort != null && !sort.isEmpty()) ? "?sort=" + sort : ""%>">Clear</a>
        <% } %>
    </form>
    <div class="toolbar">
        <a class="<%="deadline".equals(sort) ? "chip active" : "chip"%>" href="?sort=deadline<%=queryParam%>">Sort by deadline</a>
        <a class="<%="course".equals(sort) ? "chip active" : "chip"%>" href="?sort=course<%=queryParam%>">Sort by module</a>
        <a class="<%=(sort == null || sort.isEmpty()) ? "chip active" : "chip"%>" href="jobs.jsp<%=searching ? "?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()) : ""%>">Default order</a>
    </div>
    <div class="table-card">
        <% if (jobs.isEmpty()) { %>
        <p class="empty-state-message"><%=searching ? "No positions match your search" : "No positions available"%></p>
        <% } else { %>
        <table>
            <thead>
            <tr>
                <th>Position</th>
                <th>Module Code</th>
                <th>Module Name</th>
                <th>Workload</th>
                <th>Deadline</th>
                <th>Remaining Slots</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <% for (Job job : jobs) {
                PositionStatus positionStatus = ValidationUtil.getPositionStatus(job);
                boolean canApply = ValidationUtil.isJobOpenForApplications(job);
                boolean applied = DataStore.hasApplied(application, job.getId(), currentUser.getStudentId());
                ApplicationRecord myApp = applied ? DataStore.findApplicationByJobAndStudent(application, job.getId(), currentUser.getStudentId()) : null;
                String appStatus = myApp != null && myApp.getStatus() != null ? myApp.getStatus() : "PENDING";
                String deadlineDisplay = job.getDeadline().replace("T", " ");
                int vacancy = job.getRemainingSlots();
                String vacancyClass = positionStatus == PositionStatus.AVAILABLE ? "vacancy-available"
                        : (positionStatus == PositionStatus.NO_VACANCY ? "vacancy-full" : "");
            %>
            <tr>
                <td><strong><%=job.getTitle()%></strong><span class="subtle">Posted by <%=job.getTeacherName()%></span></td>
                <td><%=job.getModuleCode() == null || job.getModuleCode().isEmpty() ? "-" : job.getModuleCode()%></td>
                <td><%=job.getCourseName()%></td>
                <td><%=job.getWorkload()%></td>
                <td class="<%=positionStatus == PositionStatus.CLOSED ? "deadline-closed" : ""%>"><%=deadlineDisplay%></td>
                <td><span class="<%=vacancyClass%>"><%=vacancy%> / <%=job.getTotalSlots()%></span></td>
                <td><span class="<%=positionStatus.getCssClass()%>"><%=positionStatus.getLabel()%></span></td>
                <td>
                    <% if (applied) {
                        String statusLabel = "ACCEPTED".equals(appStatus) ? "Accepted" : ("REJECTED".equals(appStatus) ? "Rejected" : "Pending");
                    %>
                    <div class="ta-status-cell">
                        <span class="status <%=appStatus.toLowerCase()%>"><%=statusLabel%></span>
                        <span class="subtle">Applied</span>
                    </div>
                    <% } else if (canApply && positionStatus.canApply()) { %>
                    <a class="primary-btn small" href="<%=request.getContextPath()%>/ta/apply.jsp?jobId=<%=job.getId()%>">APPLY NOW</a>
                    <% } else { %>
                    <button type="button" class="disabled-btn small" disabled aria-disabled="true">APPLY NOW</button>
                    <% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } %>
    </div>
</main>
</body>
</html>
