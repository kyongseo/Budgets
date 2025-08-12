package ks.com.budgetmanagementproject.global.security;

import org.springframework.security.core.Authentication;

public class AuthUtil {

    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    public static String getUsername(Authentication authentication) {
        return isAuthenticated(authentication) ? authentication.getName() : null;
    }
}
