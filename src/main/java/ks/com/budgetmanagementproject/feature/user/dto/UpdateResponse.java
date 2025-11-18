package ks.com.budgetmanagementproject.feature.user.dto;

import ks.com.budgetmanagementproject.feature.user.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateResponse {

    private String nickname;
    private String phoneNumber;

    public static UpdateResponse from(User user) {
        return UpdateResponse.builder()
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
