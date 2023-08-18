package seoultech.capstone.menjil.domain.main.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfo;
import seoultech.capstone.menjil.domain.main.api.dto.UserInfoResponseDto;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.dto.response.UserInfo;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/main")
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping("/mentors")
    public void getMentorList() {

    }

    @GetMapping("/userinfo")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(@RequestParam("nickname") String nickname) {
        UserInfo userInfo = mainPageService.getUserInfo(nickname);
        List<RoomInfo> roomInfoList = mainPageService.getUserRoomList(nickname);

        UserInfoResponseDto userInfoResponseDto = new UserInfoResponseDto();
        userInfoResponseDto.setUserInfo(userInfo);
        userInfoResponseDto.setRoomInfoList(roomInfoList);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_USER_INFO_AVAILABLE, userInfoResponseDto));
    }

}
