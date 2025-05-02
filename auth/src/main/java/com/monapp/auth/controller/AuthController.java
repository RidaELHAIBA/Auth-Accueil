package com.monapp.auth.controller;

import com.monapp.auth.model.User;
import com.monapp.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // --- Pages login / register ---
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        authService.register(user);
        model.addAttribute("message", "Inscription réussie, vous pouvez maintenant vous connecter !");
        return "login";
    }

    // --- Redirection après login ---
    @GetMapping("/default")
    public String defaultAfterLogin() {
        return "redirect:/redirect-by-role";
    }
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/redirect-by-role")
    public String redirectByRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = auth.getName();
        authService.incrementLoginCount(email);

        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/dashboard";
        } else {
                return "redirect:http://localhost:3000";
        }
    }

    // --- Page utilisateur (USER) ---


    // --- Dashboard admin ---
    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        long totalUsers = authService.countUsers();
        long totalAdmins = authService.countAdmins();
        long totalLogins = authService.countTotalLogins();
        List<User> users = authService.getAllUsers();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalLogins", totalLogins);
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User());

        return "admin";
    }

    // --- Supprimer un utilisateur via POST sécurisé ---
    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        authService.deleteUserById(id);
        return "redirect:/dashboard";
    }

    // --- Ajouter un utilisateur depuis le dashboard ---
    @PostMapping("/admin/add")
    public String addUser(@ModelAttribute("newUser") User user) {
        authService.register(user);
        return "redirect:/dashboard";
    }
}
