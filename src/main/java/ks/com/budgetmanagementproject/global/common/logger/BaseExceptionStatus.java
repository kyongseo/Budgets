package ks.com.budgetmanagementproject.global.common.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseExceptionStatus {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "중복된 이메일이 있습니다."),
    NON_EXISTENT_USER(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    LOGIN_USER_NOT_EXIST(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    NON_EXISTENT_CATEGORY(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    DUPLICATE_BUDGET(HttpStatus.CONFLICT, "이미 설정한 예산입니다."),
    NON_EXISTENT_BUDGET(HttpStatus.NOT_FOUND, "존재하지 않는 예산입니다."),
    FORBIDDEN_USER(HttpStatus.FORBIDDEN, "권한이 없는 유저입니다."),
    NON_EXISTENT_EXPENDITURE(HttpStatus.NOT_FOUND, "존재하지 않는 지출입니다."),
    NON_EXISTENT_TOKEN(HttpStatus.NOT_FOUND, "존재하지 않는 토큰입니다."),

    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND,  "존재하지 않는 채팅방입니다."),
    CHATROOM_ALREADY_JOINED(HttpStatus.BAD_REQUEST,  "이미 채팅방에 참여 중입니다."),
    CHATROOM_NOT_MEMBER(HttpStatus.BAD_REQUEST, "채팅방에 참여 중이 아닙니다."),
    CHATROOM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "채팅방 이름은 필수입니다.");

    private final HttpStatus code;
    private final String message;
}