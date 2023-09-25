package seoultech.capstone.menjil.domain.main.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.dto.response.FollowListResponse;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/main")
public class MainPageController {

    private final MainPageService mainPageService;
    private final int pageSize = 3;

    @GetMapping("/mentors")
    public ResponseEntity<ApiResponse<Page<MentorInfoResponse>>> getMentorList(String nickname, @PageableDefault(size = pageSize, sort = {"createdDate", "nickname"},
            direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_MENTOR_LIST_AVAILABLE, mainPageService.getMentorList(nickname, pageable)));
    }

    @GetMapping("/userinfo")
    public ResponseEntity<ApiResponse<List<RoomInfoResponse>>> getAllRoomsOfUser(@RequestParam("nickname") String nickname) {
        List<RoomInfoResponse> roomInfoResponseList = mainPageService.getAllRoomsOfUser(nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_USER_ROOMS_AVAILABLE, roomInfoResponseList));
    }

    /**
     * 관심 멘토의 목록을 불러온다
     */
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FollowListResponse>>> getFollowersOfUser(@RequestParam("nickname") String nickname) {
        List<FollowListResponse> followersOfUser = mainPageService.getFollowersOfUser(nickname);
        if (isUserHasFollows(followersOfUser)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.FOLLOWS_NOT_EXISTS, followersOfUser));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.FOLLOWS_EXISTS, followersOfUser));
    }

    public boolean isUserHasFollows(List<FollowListResponse> followersOfUser) {
        return followersOfUser.isEmpty();
    }

}
