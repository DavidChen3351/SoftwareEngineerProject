package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.util.DataStore;
import com.bupt.ta.util.PasswordUtil;
import com.bupt.ta.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String role = request.getParameter("role");
        String studentId = request.getParameter("studentId");
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        if (!"TA".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Only+TA+registration+is+open");
            return;
        }
        if (DataStore.findUserByEmail(getServletContext(), email) != null) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Email+already+exists");
            return;
        }
        if (DataStore.findUserByStudentId(getServletContext(), studentId) != null) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=Student+ID+already+exists");
            return;
        }
        List<User> users = DataStore.loadUsers(getServletContext());
        users.add(new User("U" + ValidationUtil.nowStamp(), studentId, email, name,
                PasswordUtil.hash(password), "TA", true));
        DataStore.saveUsers(getServletContext(), users);
        response.sendRedirect(request.getContextPath() + "/login.jsp?success=Registration+completed");
    }
}
