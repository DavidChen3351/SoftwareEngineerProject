package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicationRecord;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.util.AuthUtil;
import com.bupt.ta.util.DataStore;
import com.bupt.ta.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@WebServlet("/ta/apply")
@MultipartConfig(maxFileSize = ApplicationServlet.MAX_RESUME_BYTES)
public class ApplicationServlet extends HttpServlet {
    public static final long MAX_RESUME_BYTES = 5L * 1024L * 1024L;
    private static final String RESUME_TOO_LARGE_MESSAGE =
            "Resume+file+is+too+large.+Please+upload+a+PDF+or+DOCX+up+to+5MB";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User student = AuthUtil.currentUser(request);
        if (student == null || !"TA".equals(student.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String jobId;
        try {
            jobId = request.getParameter("jobId");
        } catch (IllegalStateException ex) {
            redirectUploadTooLarge(request, response, null);
            return;
        }
        Job job = DataStore.findJob(getServletContext(), jobId);
        if (job == null || !ValidationUtil.isJobOpenForApplications(job)) {
            response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp?error=This+position+is+no+longer+available");
            return;
        }
        if (DataStore.hasApplied(getServletContext(), jobId, student.getStudentId())) {
            response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp?error=You+already+applied+for+this+position");
            return;
        }

        Part resume;
        try {
            resume = request.getPart("resume");
        } catch (IllegalStateException ex) {
            redirectUploadTooLarge(request, response, jobId);
            return;
        }
        if (resume == null || resume.getSize() == 0 || resume.getSubmittedFileName() == null) {
            response.sendRedirect(request.getContextPath() + "/ta/apply.jsp?jobId=" + jobId + "&error=Resume+is+required");
            return;
        }
        String originalName = Paths.get(resume.getSubmittedFileName()).getFileName().toString();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!"pdf".equals(extension) && !"docx".equals(extension)) {
            response.sendRedirect(request.getContextPath() + "/ta/apply.jsp?jobId=" + jobId + "&error=Resume+must+be+PDF+or+DOCX");
            return;
        }

        String fileName = student.getStudentId() + "_" + ValidationUtil.nowStamp() + "." + extension;
        String uploadRoot = getServletContext().getRealPath("/uploads");
        if (uploadRoot == null) {
            uploadRoot = new File("uploads").getAbsolutePath();
        }
        File uploadDir = new File(uploadRoot);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        resume.write(new File(uploadDir, fileName).getAbsolutePath());

        List<ApplicationRecord> applications = DataStore.loadApplications(getServletContext());
        applications.add(new ApplicationRecord("A" + ValidationUtil.nowStamp(), jobId, student.getStudentId(), student.getName(),
                student.getEmail(), request.getParameter("skills"), request.getParameter("availability"),
                request.getParameter("experience"), "uploads/" + fileName, "PENDING", ValidationUtil.nowDisplay()));
        DataStore.saveApplications(getServletContext(), applications);
        response.sendRedirect(request.getContextPath() + "/ta/jobs.jsp?success=Application+submitted+successfully");
    }

    private void redirectUploadTooLarge(HttpServletRequest request, HttpServletResponse response, String jobId)
            throws IOException {
        String target = "/ta/jobs.jsp?error=" + RESUME_TOO_LARGE_MESSAGE;
        if (jobId != null && !jobId.trim().isEmpty()) {
            target = "/ta/apply.jsp?jobId=" + jobId + "&error=" + RESUME_TOO_LARGE_MESSAGE;
        }
        response.sendRedirect(request.getContextPath() + target);
    }
}
