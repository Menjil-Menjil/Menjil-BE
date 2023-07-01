package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class NicknameAvailableDto {
    /**
     * nickname 중복 확인에 사용
     */
    private int status;
    private String message;

    @Builder
    public NicknameAvailableDto(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
