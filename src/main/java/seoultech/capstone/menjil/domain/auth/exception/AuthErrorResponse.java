package seoultech.capstone.menjil.domain.auth.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthErrorResponse {
    private int status;
    private String message;
    private String data;

    @Builder
    public AuthErrorResponse(int status, String message, String data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
