package seoultech.capstone.menjil.global.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonFormat(shape = JsonFormat.Shape.OBJECT) // 추가
public enum SuccessCode {

    /**
     * 200 OK
     */
    SIGNUP_AVAILABLE(HttpStatus.OK.value(), "회원가입이 가능한 이메일입니다"),
    NICKNAME_AVAILABLE(HttpStatus.OK.value(), "사용 가능한 닉네임입니다"),
    REQUEST_AVAILABLE(HttpStatus.OK.value(), "정상적으로 요청이 들어왔습니다"),
    MESSAGE_CREATED(HttpStatus.OK.value(), "채팅방 입장이 정상적으로 처리되었습니다"),
    GET_ROOMS_AND_NOT_EXISTS(HttpStatus.OK.value(), "채팅방 목록이 존재하지 않습니다"),
    GET_ROOMS_AVAILABLE(HttpStatus.OK.value(), "채팅방 목록 조회가 정상적으로 처리되었습니다"),


    /**
     * 201 CREATED
     */
    SIGNUP_SUCCESS(HttpStatus.CREATED.value(), "회원가입이 정상적으로 완료됐습니다"),
    ROOM_CREATED(HttpStatus.CREATED.value(), "채팅방이 정상적으로 생성되었습니다"),
    TOKEN_CREATED(HttpStatus.CREATED.value(), "Access Token, Refresh Token이 정상적으로 생성되었습니다"),
    MESSAGE_LOAD_SUCCESS(HttpStatus.CREATED.value(), "채팅 내역을 불러오는데 성공하였습니다");

    private final int code;
    private final String message;
}
