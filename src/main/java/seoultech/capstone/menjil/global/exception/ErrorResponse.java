package seoultech.capstone.menjil.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private int status;
    private String errorName;
    private String message; // exception message
    private String code;

    @Builder
    public ErrorResponse(int status, String errorName, String message, String code) {
        this.status = status;
        this.errorName = errorName;
        this.message = message;
        this.code = code;
    }
}
