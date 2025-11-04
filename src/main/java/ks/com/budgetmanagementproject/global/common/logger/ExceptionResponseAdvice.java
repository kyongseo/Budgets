package ks.com.budgetmanagementproject.global.common.logger;

//
//@Slf4j
//@RestControllerAdvice
//public class ExceptionResponseAdvice {
//
//    @ExceptionHandler(BaseException.class)
//    public ResponseEntity<BaseResponse> handlerBaseException(BaseException e){
//        BaseResponse response = new BaseResponse(e.getStatus().getCode().value(), e.getMessage());
//        log.error("ErrorException {}",e.getMessage());
//        return new ResponseEntity<>(response, e.getStatus().getCode());
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
//    public BaseResponse ValidExceptionHandler(BindingResult bindingResult){
//
//        List<ObjectError> errors = bindingResult.getAllErrors();
//        for(ObjectError error: errors){
//            log.info("error.getDefaultMessage() = {} ", error.getDefaultMessage());
//        }
//
//        String errorReason = errors.get(0).getDefaultMessage();
//        return new BaseResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), errorReason);
//    }
//}