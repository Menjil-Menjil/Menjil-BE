package seoultech.capstone.menjil.domain.auth.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import seoultech.capstone.menjil.domain.auth.application.AuthService;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequestDto;
import seoultech.capstone.menjil.domain.auth.dto.response.NicknameAvailableDto;
import seoultech.capstone.menjil.domain.auth.dto.response.SignUpResponseDto;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자의 SNS 회원가입 요청을 받은 뒤, db와 조회한 뒤 사용자가 있으면 Exception
     * 사용자가 없으면 register 페이지로 redirect 처리
     *
     * @param socialType
     */
    @GetMapping(value = "/signup/{socialType}")
    @ResponseBody
    public void socialSignUpType(@PathVariable(name = "socialType") String socialType,
                                 @RequestParam("email") String email) throws IOException {
        log.info(">> 사용자로부터 {} 유저가 {} SNS 회원가입 요청을 받음", email, socialType);
        authService.checkUserExistsInDb(email, socialType);
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping(value = "/check-nickname")
    public NicknameAvailableDto checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        String pattern1 = "^[가-힣a-zA-Z0-9]*$";    // 특수문자, 공백 모두 체크 가능

        if (nickname.replaceAll(" ", "").equals("")) { // 먼저 공백 확인
            throw new CustomException(ErrorCode.NICKNAME_CONTAINS_BLANK);
        }
        if (!Pattern.matches(pattern1, nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_CONTAINS_SPECIAL_CHARACTER);
        }

        return NicknameAvailableDto
                .builder()
                .status(HttpStatus.OK.value())
                .message(authService.checkNicknameDuplication(nickname))
                .build();
    }

    /**
     * 회원가입 로직
     * 사용자가 입력한 데이터를 클라이언트로부터 전달 받는다.
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SignUpResponseDto signUp(@Valid @RequestBody final SignUpRequestDto signUpRequestDto,
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
            return authService.signUp(signUpRequestDto);
        }
    }

}
