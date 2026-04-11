package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicationRecord;
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

@WebServlet("/teacher/applications/update")
public class TeacherDecisionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User teacher = AuthUtil.currentUser(request);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String applicationId = request.getParameter("applicationId");
        String status = request.getParameter("status");
        if (!"ACCEPTED".equals(status) && !"REJECTED".equals(status)) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Invalid+status");
            return;
        }
        List<ApplicationRecord> applications = DataStore.loadApplications(getServletContext());
        List<Job> jobs = DataStore.loadJobs(getServletContext());

        ApplicationRecord target = null;
        Job ownedJob = null;
        for (ApplicationRecord application : applications) {
            if (application.getId().equals(applicationId)) {
                target = application;
                break;
            }
        }
        if (target != null) {
            for (Job job : jobs) {
                if (job.getId().equals(target.getJobId()) && job.getTeacherId().equals(teacher.getId())) {
                    ownedJob = job;
                    break;
                }
            }
        }
        if (target == null || ownedJob == null) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Unauthorized+operation");
            return;
        }

        if ("ACCEPTED".equals(status) && !"ACCEPTED".equals(target.getStatus())) {
            if (ownedJob.getRemainingSlots() <= 0) {
                response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=No+remaining+slots");
                return;
            }
            ownedJob.setFilledSlots(ownedJob.getFilledSlots() + 1);
        }
        if (!"ACCEPTED".equals(status) && "ACCEPTED".equals(target.getStatus())) {
            ownedJob.setFilledSlots(Math.max(0, ownedJob.getFilledSlots() - 1));
        }

        target.setStatus(status);
        DataStore.saveApplications(getServletContext(), applications);
        DataStore.saveJobs(getServletContext(), jobs);
        response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?success=Application+updated");
    }
}
