package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.util.AuthUtil;
import com.bupt.ta.util.DataStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/teacher/jobs/listing")
public class JobListingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User teacher = AuthUtil.currentUser(request);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String jobId = request.getParameter("jobId");
        String acceptingParam = request.getParameter("accepting");
        if (jobId == null || jobId.trim().isEmpty() || acceptingParam == null) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Invalid+request");
            return;
        }
        boolean accepting = "true".equalsIgnoreCase(acceptingParam.trim());

        List<Job> jobs = DataStore.loadJobs(getServletContext());
        Job target = null;
        for (Job job : jobs) {
            if (job.getId().equals(jobId.trim())) {
                target = job;
                break;
            }
        }
        if (target == null || !teacher.getId().equals(target.getTeacherId())) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Unauthorized+or+unknown+position");
            return;
        }

        target.setAcceptingApplications(accepting);
        DataStore.saveJobs(getServletContext(), jobs);
        String message = accepting ? "Applications+resumed+for+this+position" : "Applications+paused+for+this+position";
        response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?success=" + message);
    }
}
