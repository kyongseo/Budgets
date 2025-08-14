package ks.com.budgetmanagementproject.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseExceptionStatus {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일이 있습니다."),
    NON_EXISTENT_USER(HttpStatus.BAD_REQUEST, "존재하지 않는 유저입니다."),
    LOGIN_USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    NON_EXISTENT_CATEGORY(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."),
    DUPLICATE_BUDGET(HttpStatus.CONFLICT, "이미 설정한 예산입니다."),
    NON_EXISTENT_BUDGET(HttpStatus.BAD_REQUEST, "존재하지 않는 예산입니다."),
    FORBIDDEN_USER(HttpStatus.FORBIDDEN, "권한이 없는 유저입니다."),
    NON_EXISTENT_EXPENDITURE(HttpStatus.BAD_REQUEST, "존재하지 않는 지출입니다.");

    private final HttpStatus code;
    private final String message;
}