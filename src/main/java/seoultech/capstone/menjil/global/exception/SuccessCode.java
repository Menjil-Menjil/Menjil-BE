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
    // auth
    SIGNUP_AVAILABLE(HttpStatus.OK.value(), "회원가입이 가능한 이메일입니다"),
    NICKNAME_AVAILABLE(HttpStatus.OK.value(), "사용 가능한 닉네임입니다"),
    REQUEST_AVAILABLE(HttpStatus.OK.value(), "정상적으로 요청이 들어왔습니다"),

    // chat
    MESSAGE_CREATED(HttpStatus.OK.value(), "채팅방 입장이 정상적으로 처리되었습니다"),
    GET_ROOMS_AND_NOT_EXISTS(HttpStatus.OK.value(), "채팅방 목록이 존재하지 않습니다"),
    GET_ROOMS_AVAILABLE(HttpStatus.OK.value(), "채팅방 목록을 불러오는데 성공하였습니다"),

    // main
    GET_USER_ROOMS_AVAILABLE(HttpStatus.OK.value(), "사용자의 채팅방 목록을 불러오는데 성공하였습니다"),
    GET_MENTOR_LIST_AVAILABLE(HttpStatus.OK.value(), "멘토 리스트를 불러오는데 성공하였습니다"),
    FOLLOWS_NOT_EXISTS(HttpStatus.OK.value(), "관심 멘토 목록이 존재하지 않습니다"),
    FOLLOWS_EXISTS(HttpStatus.OK.value(), "관심 멘토 목록을 불러오는데 성공하였습니다"),

    // follow
    FOLLOW_CHECK_SUCCESS(HttpStatus.OK.value(), "팔로우 조회에 성공하셨습니다"),


    /**
     * 201 CREATED
     */
    // auth
    SIGNUP_SUCCESS(HttpStatus.CREATED.value(), "회원가입이 정상적으로 완료됐습니다"),
    TOKEN_CREATED(HttpStatus.CREATED.value(), "Access Token, Refresh Token이 정상적으로 생성되었습니다"),

    // chat
    ROOM_DELETE_SUCCESS(HttpStatus.CREATED.value(), "채팅방 퇴장이 정상적으로 처리되었습니다"),
    MESSAGE_SEND_SUCCESS(HttpStatus.CREATED.value(), "채팅 메시지가 정상적으로 입력되었습니다"),
    AI_QUESTION_RESPONSE(HttpStatus.CREATED.value(), "AI 챗봇 응답입니다"),

    // follow
    FOLLOW_CREATED(HttpStatus.CREATED.value(), "팔로우가 정상적으로 생성되었습니다"),
    FOLLOW_DELETED(HttpStatus.CREATED.value(), "팔로우가 정상적으로 제거되었습니다"),

    /**
     * 202 ACCEPTED
     * 아래 응답은 202 코드와 맞지 않지만, 프로젝트에서 단순 구분을 위해 수정하였음
     */
    MESSAGE_LOAD_SUCCESS(HttpStatus.ACCEPTED.value(), "채팅 내역을 불러오는데 성공하였습니다");
    private final int code;
    private final String message;
}
