package ks.com.budgetmanagementproject.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEditDto {

    @NotBlank(message = "닉네임은 필수로 입력되어야 합니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Pattern(message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.", regexp = "^[가-힣a-zA-Z0-9]+$")
    private String nickname;

    @NotBlank(message = "전화번호는 필수로 입력되어야 합니다.")
    @Pattern(message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)", regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")
    private String phoneNumber;
}
