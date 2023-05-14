package seoultech.capstone.menjil.domain.user.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import seoultech.capstone.menjil.domain.user.application.UserService;
import seoultech.capstone.menjil.domain.user.dto.response.UserAcceptDtoRes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * 닉네임 중복 체크
     */
    @GetMapping("/check-nickname")
    @ResponseBody
    public UserAcceptDtoRes checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        log.info("checked nickname = {}", nickname);
        return UserAcceptDtoRes
                .builder()
                .status(HttpStatus.OK)
                .message(userService.checkNicknameDuplication(nickname))
                .build();
    }
}
