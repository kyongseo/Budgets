package ks.com.budgetmanagementproject.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResDto {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
}
