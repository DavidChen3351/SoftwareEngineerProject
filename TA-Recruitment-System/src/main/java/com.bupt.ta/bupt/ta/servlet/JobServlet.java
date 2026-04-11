package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.util.AuthUtil;
import com.bupt.ta.util.DataStore;
import com.bupt.ta.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/teacher/jobs/create")
public class JobServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User teacher = AuthUtil.currentUser(request);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String title = request.getParameter("title");
        String courseName = request.getParameter("courseName");
        String workload = request.getParameter("workload");
        int totalSlots = Integer.parseInt(request.getParameter("totalSlots"));
        String deadline = request.getParameter("deadline");

        if (!ValidationUtil.isFutureOrToday(deadline) || totalSlots < 1) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?error=Invalid+job+settings");
            return;
        }

        List<Job> jobs = DataStore.loadJobs(getServletContext());
        jobs.add(new Job("J" + ValidationUtil.nowStamp(), title, courseName, workload, totalSlots, 0, deadline,
                teacher.getId(), teacher.getName()));
        DataStore.saveJobs(getServletContext(), jobs);
        response.sendRedirect(request.getContextPath() + "/teacher/dashboard.jsp?success=Position+published");
    }
}
