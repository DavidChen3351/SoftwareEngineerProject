<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.bupt.ta.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    if ("TA".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp");
        return;
    }
    if ("TEACHER".equals(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp");
        return;
    }
    response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
%>
