package seoultech.capstone.menjil.domain.follow.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seoultech.capstone.menjil.domain.follow.api.dto.request.FollowCreateRequest;
import seoultech.capstone.menjil.domain.follow.application.FollowService;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import javax.validation.Valid;

import static seoultech.capstone.menjil.global.common.dto.ApiResponse.success;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.FOLLOW_CREATED;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/follow")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createFollow(@Valid @RequestBody FollowCreateRequest followCreateRequest) {

        int result = followService.createFollow(followCreateRequest.toServiceRequest());
        if (result == FOLLOW_CREATED.getValue()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(success(SuccessCode.FOLLOW_CREATED));
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(success(SuccessCode.FOLLOW_DELETED));
        }
    }

    @GetMapping("/check-status")
    public ResponseEntity<ApiResponse<Boolean>> checkFollowStatus(@RequestParam("userNickname") String userNickname,
                                                                  @RequestParam("followNickname") String followNickname) {
        boolean FOLLOW_EXISTS = true;
        boolean FOLLOW_NOT_EXISTS = false;

        boolean result = followService.checkFollowStatus(userNickname, followNickname);
        if (result == FOLLOW_EXISTS) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(success(SuccessCode.FOLLOW_CHECK_SUCCESS, FOLLOW_EXISTS));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(success(SuccessCode.FOLLOW_CHECK_SUCCESS, FOLLOW_NOT_EXISTS));
        }
    }
}
