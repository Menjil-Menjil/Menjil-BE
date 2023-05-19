package seoultech.capstone.menjil.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import seoultech.capstone.menjil.global.exception.ErrorCode;

@Getter
@AllArgsConstructor
public class CustomAuthException extends RuntimeException {
    private final ErrorCode errorCode;
}
