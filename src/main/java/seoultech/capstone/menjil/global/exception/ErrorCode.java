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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류"),

    // auth
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "A001", "이미 존재하는 닉네임입니다"),
    USER_DUPLICATED(HttpStatus.CONFLICT, "A002", "이미 가입된 사용자입니다"),
    NICKNAME_CONTAINS_BLANK(HttpStatus.BAD_REQUEST, "A003", "닉네임에 공백을 사용할 수 없습니다"),
    NICKNAME_CONTAINS_SPECIAL_CHARACTER(HttpStatus.BAD_REQUEST, "A004", "닉네임에 특수 문자를 사용할 수 없습니다"),
    SIGNUP_INPUT_INVALID(HttpStatus.BAD_REQUEST, "A005", "Input type is invalid"),
    PROVIDER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "A006", "가입 양식이 잘못되었습니다. 구글이나 카카오로 가입 요청을 해주세요"),
    USER_NOT_EXISTED(HttpStatus.BAD_REQUEST, "A007", "가입된 사용자가 존재하지 않습니다"),

    // main
    NICKNAME_NOT_EXISTED(HttpStatus.CONFLICT, "m001", "잘못된 닉네임을 요청하셨습니다"),

    // chat
    ROOM_NOT_EXISTED(HttpStatus.BAD_REQUEST, "CH01", "채팅방의 정보를 가져올 수 없습니다. 채팅방 아이디를 다시 확인해주세요"),
    TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CH02", "사용자의 타입이 \"MENTEE\" 또는 \"MENTOR\" 가 아닙니다"),
    TIME_INPUT_INVALID(HttpStatus.BAD_REQUEST, "CH03", "time 형식을 yyyy-MM-dd HH:mm:ss으로 작성해 주세요"),
    MESSAGE_TYPE_INPUT_INVALID(HttpStatus.CONFLICT, "CH04", "지정된 Message Type이 입력되지 않았습니다"),
    MENTEE_NICKNAME_NOT_EXISTED(HttpStatus.BAD_REQUEST, "CH05", "멘티의 닉네임 정보가 존재하지 않습니다"),
    MENTOR_NICKNAME_NOT_EXISTED(HttpStatus.BAD_REQUEST, "CH06", "멘토의 닉네임 정보가 존재하지 않습니다"),
    QALIST_NOT_EXISTED(HttpStatus.BAD_REQUEST, "CH07", "채팅 메시지 id가 유효하지 않습니다");

    private final HttpStatus httpStatus;
    private final String type;
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }
}
