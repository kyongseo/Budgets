package ks.com.budgetmanagementproject.global.common.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseResponseStatus {

    SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),

    // user
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인이 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃이 완료되었습니다."),
    ACCESS_TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "액세스 토큰이 재발급되었습니다."),
    USER_UPDATE_SUCCESS(HttpStatus.OK, "사용자 정보 수정이 완료되었습니다."),

    // Budget
    BUDGET_CREATE_SUCCESS(HttpStatus.CREATED, "예산이 등록되었습니다."),
    BUDGET_UPDATE_SUCCESS(HttpStatus.OK, "예산이 수정되었습니다."),
    BUDGET_DELETE_SUCCESS(HttpStatus.OK, "예산이 삭제되었습니다."),
    BUDGET_RECOMMEND_SUCCESS(HttpStatus.OK, "예산이 추천되었습니다."),
    BUDGET_CATEGORY_LIST_SUCCESS(HttpStatus.OK, "예산 카테고리 목록 조회에 성공했습니다."),

    // Expenditure
    EXPENDITURE_CREATE_SUCCESS(HttpStatus.CREATED, "지출이 생성되었습니다."),
    EXPENDITURE_UPDATE_SUCCESS(HttpStatus.OK, "지출이 수정되었습니다."),
    EXPENDITURE_LIST_SUCCESS(HttpStatus.OK, "지출 목록 조회에 성공했습니다."),
    EXPENDITURE_DETAIL_SUCCESS(HttpStatus.OK, "지출 상세 조회에 성공했습니다."),
    EXPENDITURE_SOFT_DELETE_SUCCESS(HttpStatus.OK, "지출 삭제(soft)에 성공했습니다."),
    EXPENDITURE_HARD_DELETE_SUCCESS(HttpStatus.OK, "지출 삭제(hard)에 성공했습니다."),
    EXPENDITURE_EXCEPT_UPDATE_SUCCESS(HttpStatus.OK, "지출 합계 제외 업데이트에 성공했습니다."),
    EXPENDITURE_RECOMMEND_SUCCESS(HttpStatus.OK, "지출 추천에 성공했습니다."),
    EXPENDITURE_GUIDE_SUCCESS(HttpStatus.OK, "지출 안내 조회에 성공했습니다."),

    // ChatRoom
    CHATROOM_CREATE_SUCCESS(HttpStatus.CREATED,  "채팅방이 생성되었습니다."),
    CHATROOM_LIST_SUCCESS(HttpStatus.OK,  "채팅방 목록 조회에 성공했습니다."),
    CHATROOM_DETAIL_SUCCESS(HttpStatus.OK,  "채팅방 상세 조회에 성공했습니다."),
    CHATROOM_JOIN_SUCCESS(HttpStatus.OK, "채팅방에 입장했습니다."),
    CHATROOM_LEAVE_SUCCESS(HttpStatus.OK, "채팅방에서 나갔습니다.");





    private final HttpStatus status;
    private final String message;
}
