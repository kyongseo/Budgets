package ks.com.budgetmanagementproject.feature.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpReqDto  {


    @NotBlank(message = "이메일은 필수로 입력되어야 합니다.")
    @Email(message = "유효하지 않은 이메일 형식입니다.",
            regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
    private String username;

    @NotBlank(message = "비밀번호는 필수로 입력되어야 합니다.")
    @Pattern(message = "비밀번호는 4~16자에 영어, 숫자, 특수문자가 포함된 형태로 공백 없이 작성되어야 합니다.",
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{4,16}")
    private String password;
}
