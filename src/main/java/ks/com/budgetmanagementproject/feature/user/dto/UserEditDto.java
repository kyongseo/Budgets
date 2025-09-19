package ks.com.budgetmanagementproject.feature.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEditDto {

    private String usernick;
    private String phoneNumber;
}
