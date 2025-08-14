package ks.com.budgetmanagementproject.global.common;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException{

    private final BaseExceptionStatus status;

    public BaseException(BaseExceptionStatus status){
        super(status.getMessage());
        this.status = status;
    }
}