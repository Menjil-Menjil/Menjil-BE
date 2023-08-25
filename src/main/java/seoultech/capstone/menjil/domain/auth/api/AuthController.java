package seoultech.capstone.menjil.domain.auth.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import seoultech.capstone.menjil.domain.auth.application.AuthService;
import seoultech.capstone.menjil.domain.auth.dto.request.SignInRequest;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequest;
import seoultech.capstone.menjil.domain.auth.dto.response.SignInResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import javax.validation.Valid;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static seoultech.capstone.menjil.global.common.dto.ApiResponse.success;
import static seoultech.capstone.menjil.global.exception.ErrorCode.*;
import static seoultech.capstone.menjil.global.exception.SuccessCode.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자의 SNS 회원가입 요청을 받은 뒤, db와 조회한 뒤 사용자가 있으면 Exception
     * 사용자가 없으면 register 페이지로 redirect 처리
     */
    @GetMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> checkSignupIsAvailable(@RequestParam("email") String email,
                                                                 @RequestParam("provider") String provider) {
        log.info(">> 사용자로부터 {} 유저가 {} 회원가입 가능 여부 조회를 요청 받음", email, provider);
        int httpStatusValue = authService.checkUserExistsInDb(email, provider);
        if (httpStatusValue == HttpStatus.OK.value()) {
            return ResponseEntity.status(HttpStatus.OK).body(success(SIGNUP_AVAILABLE));
//          return new ResponseEntity<ApiResponse<?>>(success(SuccessCode.SIGNUP_AVAILABLE), HttpStatus.OK);
        } else {
            throw new CustomException(USER_DUPLICATED);
        }
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping(value = "/check-nickname")
    public ResponseEntity<ApiResponse<?>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        String pattern1 = "^[가-힣a-zA-Z0-9]*$";    // 특수문자, 공백 모두 체크 가능

        if (nickname.replaceAll(" ", "").equals("")) { // 먼저 공백 확인
            throw new CustomException(NICKNAME_CONTAINS_BLANK);
        }
        if (!Pattern.matches(pattern1, nickname)) {
            throw new CustomException(NICKNAME_CONTAINS_SPECIAL_CHARACTER);
        }
        int httpStatusValue = authService.checkNicknameDuplication(nickname);

        if (httpStatusValue == HttpStatus.OK.value()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(success(NICKNAME_AVAILABLE));
        } else {
            throw new CustomException(NICKNAME_DUPLICATED);
        }
    }

    /**
     * 회원가입 로직
     * 사용자가 입력한 데이터를 클라이언트로부터 전달 받는다.
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> signUp(@Valid @RequestBody final SignUpRequest signUpRequest,
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

            CustomException exception = new CustomException(SIGNUP_INPUT_INVALID);
            exception.getErrorCode().setMessage(sb.toString()); // 필드 오류를 메시지로 설정
            throw exception;
        }

        return ResponseEntity.status(authService.signUp(signUpRequest))
                .body(success(SIGNUP_SUCCESS, signUpRequest.getEmail()));
    }

    /**
     * 로그인 로직. NextAuth.js 에서 플랫폼 서버 인증 후 유저 정보를 가져오면,
     * 서버로 전송해서 유저가 db 에도 등록이 되어 있는지를 검증한다.
     * email, provider 로 검증
     * 기존에 가입된 유저가 있으면, access & refresh token 전달
     * 가입된 유저가 없으면, CustomException 처리
     */
    @PostMapping(value = "/signin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(@RequestBody SignInRequest dto) {

        String provider = dto.getProvider();

        if (!provider.equals("google") && !provider.equals("kakao")) {
            throw new CustomException(PROVIDER_NOT_ALLOWED);
        }

        SignInResponse signInResponse = authService.signIn(dto.getEmail(),
                dto.getProvider());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.TOKEN_CREATED, signInResponse));
    }
}
