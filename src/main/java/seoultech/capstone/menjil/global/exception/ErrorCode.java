package seoultech.capstone.menjil.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method not allowed"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),

    // auth
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "A001", "Nickname is already existed"),
    USER_DUPLICATED(HttpStatus.CONFLICT, "A002", "User is already existed"),
    NICKNAME_CONTAINS_BLANK(HttpStatus.BAD_REQUEST, "A003", "Nickname only contains blank"),
    NICKNAME_CONTAINS_SPECIAL_CHARACTER(HttpStatus.BAD_REQUEST, "A004", "Nickname cannot contains special character"),
    SIGNUP_INPUT_INVALID(HttpStatus.BAD_REQUEST, "A005", "Input type is invalid"),
    PROVIDER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "A006", "가입 양식이 잘못되었습니다. 구글이나 카카오로 가입 요청을 해주세요"),
    USER_NOT_EXISTED(HttpStatus.BAD_REQUEST, "A007", "가입된 사용자가 존재하지 않습니다");


    private final HttpStatus httpStatus;
    private final String code;
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }
}
