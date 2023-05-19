package seoultech.capstone.menjil.domain.user.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import seoultech.capstone.menjil.domain.user.application.UserService;
import seoultech.capstone.menjil.domain.user.dto.request.UserRequestDto;
import seoultech.capstone.menjil.domain.user.dto.response.UserAcceptResponseDto;
import seoultech.capstone.menjil.domain.user.dto.response.UserSignupResponseDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.validation.Valid;
import java.util.concurrent.atomic.AtomicInteger;
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

    /**
     * 회원가입 요청 처리
     * 사용자가 입력한 데이터를 클라이언트로부터 전달 받는다.
     * data는 JWT decoding 과정을 거쳐야 한다.
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserSignupResponseDto signUp(@Valid @RequestBody final UserRequestDto requestDto,
                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            // 특정 field error 의 값을 담는다.
            StringBuilder sb = new StringBuilder();
            AtomicInteger i = new AtomicInteger(1);
            bindingResult.getFieldErrors()
                    .forEach(fieldError -> {
                        sb.append(i.get()).append(". ").append(fieldError.getDefaultMessage()).append(" ");
                        i.getAndIncrement();
                    });

            CustomException exception = new CustomException(ErrorCode.SIGNUP_INPUT_INVALID);
            exception.getErrorCode().setMessage(sb.toString()); // 필드 오류를 메시지로 설정
            throw exception;
        } else {
            return userService.signUp(requestDto);
        }
    }
}
