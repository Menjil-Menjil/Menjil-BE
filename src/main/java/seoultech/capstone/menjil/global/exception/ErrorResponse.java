package seoultech.capstone.menjil.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private int status;
    private String message; // exception message
    private String type;

    @Builder
    public ErrorResponse(int status, String message, String type) {
        this.status = status;
        this.message = message;
        this.type = type;
    }
}
