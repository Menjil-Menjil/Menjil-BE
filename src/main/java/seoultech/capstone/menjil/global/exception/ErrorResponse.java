package seoultech.capstone.menjil.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final int status;
    private final String message; // exception message
    private final String type;

    @Builder
    public ErrorResponse(int status, String message, String type) {
        this.status = status;
        this.message = message;
        this.type = type;
    }
}
