package seoultech.capstone.menjil.domain.auth.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpCheckUserDto {
    private int status;
    private String message;

    @Builder
    public SignUpCheckUserDto(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
