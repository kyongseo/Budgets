package ks.com.budgetmanagementproject.feature.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final JWTUtil jwtUtil;

    @GetMapping("/chat")
    public String chatPage(Model model, HttpServletRequest request) {
        String accessToken = jwtUtil.getAccessTokenFromCookies(request);

        String username = "익명";
        if (accessToken != null && !jwtUtil.isExpiredDate(accessToken)) {
            username = jwtUtil.getUsername(accessToken);
        }

        model.addAttribute("username", username);
        return "chat";
    }
}