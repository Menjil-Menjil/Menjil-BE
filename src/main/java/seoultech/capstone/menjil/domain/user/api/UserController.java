package seoultech.capstone.menjil.domain.user.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seoultech.capstone.menjil.domain.user.application.UserService;
import seoultech.capstone.menjil.domain.user.dto.response.UserAcceptResponseDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.util.regex.Pattern;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * 닉네임 중복 체크
     */
    @GetMapping("/check-nickname")
    public UserAcceptResponseDto checkNicknameDuplicate(@RequestParam("nickname") String nickname) {

        String pattern1 = "^[가-힣a-zA-Z0-9]*$";    // 특수문자, 공백 모두 체크 가능

        if (nickname.replaceAll(" ", "").equals("")) { // 먼저 공백 확인
            throw new CustomException(ErrorCode.NICKNAME_CONTAINS_BLANK);
        }
        if (!Pattern.matches(pattern1, nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER);
        }

        return UserAcceptResponseDto
                .builder()
                .status(HttpStatus.OK)
                .message(userService.checkNicknameDuplication(nickname))
                .build();
    }

}
