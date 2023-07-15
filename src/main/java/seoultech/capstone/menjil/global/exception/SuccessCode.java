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

    /**
     * 201 CREATED
     */
    SIGNUP_SUCCESS(HttpStatus.CREATED.value(), "회원가입이 정상적으로 완료됐습니다");

    private final int code;
    private final String message;
}
