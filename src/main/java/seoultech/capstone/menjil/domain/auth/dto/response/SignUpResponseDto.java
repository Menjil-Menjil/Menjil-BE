package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.Getter;
import seoultech.capstone.menjil.domain.auth.domain.User;

@Getter
public class SignUpResponseDto {
    private int status;
    private String email;
    private String message;

    public SignUpResponseDto(User user) {
        this.status = 201;  // created
        this.email = user.getEmail();
        this.message = "가입 요청이 정상적으로 처리되었습니다";
    }
}