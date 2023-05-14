package seoultech.capstone.menjil.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", " Method not allowed"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),

    // User
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "U001", "Nickname is already existed"),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "U002", "Login input is invalid. " +
            "Please check your email or password");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
