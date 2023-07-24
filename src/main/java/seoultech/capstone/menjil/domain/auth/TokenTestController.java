package seoultech.capstone.menjil.domain.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

@RestController
public class TokenTestController {
    /**
     * JWT 토큰 검증 절차 테스트를 위해 작성한 코드
     */

    @PostMapping("/api/user/token-test")
    public ResponseEntity<ApiResponse<String>> test() {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.REQUEST_AVAILABLE, "정상 요청"));
    }
}