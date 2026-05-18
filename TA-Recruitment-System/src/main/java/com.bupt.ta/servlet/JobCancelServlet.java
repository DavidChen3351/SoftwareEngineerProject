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

@WebServlet("/teacher/jobs/cancel")
public class JobCancelServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User teacher = AuthUtil.currentUser(request);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String jobId = request.getParameter("jobId");
        if (jobId == null || jobId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Invalid+request");
            return;
        }

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
        if (target.isCancelled()) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Position+already+cancelled");
            return;
        }

        target.setCancelled(true);
        target.setAcceptingApplications(false);
        DataStore.saveJobs(getServletContext(), jobs);
        response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?success=Position+cancelled+and+removed+from+TA+portal");
    }
}
