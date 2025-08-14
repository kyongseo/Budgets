package ks.com.budgetmanagementproject.global.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionResponseAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse> handlerBaseException(BaseException e){
        BaseResponse response = new BaseResponse(e.getStatus().getCode().value(), e.getMessage());
        log.error("ErrorException {}",e.getMessage());
        return new ResponseEntity<>(response, e.getStatus().getCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    public BaseResponse ValidExceptionHandler(BindingResult bindingResult){

        List<ObjectError> errors = bindingResult.getAllErrors();
        for(ObjectError error: errors){
            log.info("error.getDefaultMessage() = {} ", error.getDefaultMessage());
        }

        String errorReason = errors.get(0).getDefaultMessage();
        return new BaseResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), errorReason);
    }
}