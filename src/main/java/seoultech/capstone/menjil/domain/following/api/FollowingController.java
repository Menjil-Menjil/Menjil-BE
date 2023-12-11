package seoultech.capstone.menjil.domain.following.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seoultech.capstone.menjil.domain.following.application.FollowingService;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserInfoResponse;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/following")
public class FollowingController {

    private final FollowingService followingService;
    private final int PAGE_SIZE = 9;

    @GetMapping()
    public ResponseEntity<ApiResponse<Page<FollowingUserResponse>>> getAllFollowOfUsers(
            @RequestParam("nickname") String nickname,
            @PageableDefault(size = PAGE_SIZE, sort = {"createdDate"},
                    direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_ALL_FOLLOW_USERS_SUCCESS,
                        followingService.getAllFollowOfUsers(nickname, pageable)));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<FollowingUserInfoResponse>> getFollowUserInfo(
            @RequestParam("followNickname") String followNickname) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_FOLLOW_USER_INFO_SUCCESS,
                        followingService.getFollowUserInfo(followNickname)));
    }
}
