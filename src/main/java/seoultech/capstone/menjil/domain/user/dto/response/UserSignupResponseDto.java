package seoultech.capstone.menjil.domain.user.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import seoultech.capstone.menjil.domain.user.domain.User;

@Getter
public class UserSignupResponseDto {
    private int status;
    private String email;
    private String name;
    private String message;

    public UserSignupResponseDto(User user) {
        this.status = 201;  // created
        this.email = user.getEmail();
        this.name = user.getName();
        this.message = "가입 요청이 정상적으로 처리되었습니다";
    }
}
