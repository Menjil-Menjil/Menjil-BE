package seoultech.capstone.menjil.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@AllArgsConstructor
public class OAuthUserResponseDto {
    private final HttpStatus status;
    private final String message;
    private final String data;
}
