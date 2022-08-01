package ti.gateway.dashboard.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import ti.gateway.dashboard.base.ErrorCode;
import ti.gateway.dashboard.base.Result;

public class ExceptionAdviceHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionAdviceHandler.class);

    @ExceptionHandler(Throwable.class)
    public Result<?> handleError(ServerHttpRequest request, Exception ex) {
        log.error("Exception uri:{} ex:{} exDetail:{}", request.getPath(), ex.toString(), ex);
        return new Result<>(ErrorCode.InternalError.getValue(), ex.getMessage());
    }


    @ExceptionHandler(BindException.class)
    public Result<?> parameterError(BindException e, ServerHttpRequest request) {
        log.warn("Exception uri:{} ex:{} exDetail:{}", request.getPath(), e.toString(), e);
        return new Result<>(ErrorCode.ParameterError.getValue(), e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Result<?> parameterError(WebExchangeBindException e, ServerHttpRequest request) {
        log.warn("Exception uri:{} ex:{} exDetail:{}", request.getPath(), e.toString(), e);
        return new Result<>(ErrorCode.ParameterError.getValue(), e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidException(MethodArgumentNotValidException e, ServerHttpRequest request) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        log.warn("Exception uri:{} field:{} ex:{} exDetail:", request.getPath(),
                fieldError.getField(), fieldError.getDefaultMessage(), e);
        return new Result<>(ErrorCode.ParameterError.getValue(), fieldError.getDefaultMessage());
    }

}
