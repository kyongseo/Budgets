package ks.com.budgetmanagementproject.global.common.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "message", "result"})
public class BaseResponse<T> {

    private final int code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    public static <T> BaseResponse<T> of(BaseResponseStatus status, T data) {
        return new BaseResponse<>(
                status.getStatus().value(),
                status.getMessage(),
                data
        );
    }

    public static BaseResponse<Void> of(BaseResponseStatus status) {
        return new BaseResponse<>(
                status.getStatus().value(),
                status.getMessage(),
                null
        );
    }
}