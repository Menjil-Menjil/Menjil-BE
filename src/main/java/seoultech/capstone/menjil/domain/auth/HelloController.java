package seoultech.capstone.menjil.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

@RestController
public class HelloController {

    @PostMapping("/api/user/hello")
    public ResponseEntity<ApiResponse> test() {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.NICKNAME_AVAILABLE, 33));
    }
}
