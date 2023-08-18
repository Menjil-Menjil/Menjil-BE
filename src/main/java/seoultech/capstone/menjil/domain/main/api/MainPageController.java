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
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoDto;
import seoultech.capstone.menjil.domain.main.api.dto.UserInfoResponseDto;
import seoultech.capstone.menjil.domain.main.application.MainPageService;
import seoultech.capstone.menjil.domain.main.dto.response.MentorInfoDto;
import seoultech.capstone.menjil.domain.main.dto.response.UserInfoDto;
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
    public ResponseEntity<ApiResponse<Page<MentorInfoDto>>> getMentorList(@PageableDefault(size = 3, sort = {"createdDate", "nickname"},
            direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_MENTOR_LIST_AVAILABLE, mainPageService.getMentorList(pageable)));
    }

    @GetMapping("/userinfo")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(@RequestParam("nickname") String nickname) {
        UserInfoDto userInfoDto = mainPageService.getUserInfo(nickname);
        List<RoomInfoDto> roomInfoDtoList = mainPageService.getUserRoomList(nickname);

        UserInfoResponseDto userInfoResponseDto = new UserInfoResponseDto();
        userInfoResponseDto.setUserInfoDto(userInfoDto);
        userInfoResponseDto.setRoomInfoDtoList(roomInfoDtoList);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.GET_USER_INFO_AVAILABLE, userInfoResponseDto));
    }

}
