package seoultech.capstone.menjil.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserAcceptDtoRes {
    /**
     * nickname 중복 확인, 회원가입 로직 등에 사용.
     */
    private HttpStatus status;
    private String message;

    @Builder
    public UserAcceptDtoRes(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
