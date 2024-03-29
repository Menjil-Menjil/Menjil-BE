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
import seoultech.capstone.menjil.domain.chatbot.application.ChatBotRoomService;
import seoultech.capstone.menjil.domain.chatbot.dto.response.RoomInfoResponse;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.application.dto.response.FollowUserResponse;
import seoultech.capstone.menjil.domain.main.application.dto.response.UserInfoResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/main")
public class MainPageController {

    private final ChatBotRoomService chatBotRoomService;
    private final MainPageService mainPageService;
    private final int pageSize = 3;

    @GetMapping("/mentors")
    public ResponseEntity<ApiResponse<Page<UserInfoResponse>>> getMentors(String nickname, @PageableDefault(size = pageSize, sort = {"createdDate", "nickname"},
            direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_USERS_AVAILABLE, mainPageService.getMentors(nickname, pageable)));
    }

//    @GetMapping("/rooms")
//    public ResponseEntity<ApiResponse<List<RoomInfoResponse>>> getAllChatBotRooms(@RequestParam("nickname") String nickname,
//                                                                                 @RequestParam("type") String type) {
//        List<RoomInfoResponse> result = roomService.getAllChatBotRooms(nickname, type);
//
//        if (userHasNoRooms(result)) {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(ApiResponse.success(SuccessCode.NO_ROOMS_AVAILABLE, result));
//        } else {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(ApiResponse.success(SuccessCode.GET_ROOMS_AVAILABLE, result));
//        }
//    }

    /**
     * 관심 멘토의 목록을 불러온다
     */
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FollowUserResponse>>> getFollowersOfUser(@RequestParam("nickname") String nickname) {
        List<FollowUserResponse> followersOfUser = mainPageService.getFollowersOfUser(nickname);
        if (isUserHasFollows(followersOfUser)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.FOLLOWS_EXISTS, followersOfUser));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.FOLLOWS_NOT_EXISTS, followersOfUser));
    }

    private boolean userHasNoRooms(List<RoomInfoResponse> list) {
        return list.isEmpty();
    }

    private boolean isUserHasFollows(List<FollowUserResponse> followersOfUser) {
        return !followersOfUser.isEmpty();
    }
}
