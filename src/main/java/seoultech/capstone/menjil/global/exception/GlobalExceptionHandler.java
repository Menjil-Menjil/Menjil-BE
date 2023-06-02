package seoultech.capstone.menjil.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import seoultech.capstone.menjil.domain.auth.exception.AuthErrorResponse;
import seoultech.capstone.menjil.domain.auth.exception.CustomAuthException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("handleCustomException throw CustomException : {}", e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(e.getErrorCode().getHttpStatus().value())
                        .errorName(e.getErrorCode().getHttpStatus().name())
                        .message(e.getErrorCode().getMessage())
                        .code(e.getErrorCode().getCode())
                        .build());
    }

    @ExceptionHandler(value = {CustomAuthException.class})
    public ResponseEntity<AuthErrorResponse> handleCustomAuthException(CustomAuthException e) {
        log.error("handleCustomAuthException throw CustomAuthException : {}", e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(AuthErrorResponse.builder()
                        .status(e.getErrorCode().getHttpStatus().value())
                        .message(e.getErrorCode().getMessage())
                        .data(null)
                        .build());
    }
}
