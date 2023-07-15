package seoultech.capstone.menjil.domain.auth.application;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequestDto;
import seoultech.capstone.menjil.domain.auth.dto.response.SignInResponseDto;
import seoultech.capstone.menjil.domain.auth.dto.response.SignUpResponseDto;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;
import seoultech.capstone.menjil.global.exception.CustomException;

import javax.crypto.SecretKey;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml" +
        ",classpath:/application-database-test.yml" +
        ",classpath:/application-jwt.properties")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.secret.token}")
    private String secretKey;
    private SecretKey TEST_JWT_SECRET_TOKEN_KEY;

    @BeforeEach
    void init() {
        // @Test , @RepeatedTest , @ParameterizedTest , @TestFactory 가 붙은 테스트 메소드가 실행하기 전에 실행된다.
        // 각 테스트 메서드 전에 실행된다.
        User userA = createUser("google_1", "userA@gmail.com", "google", "testA");
        userRepository.save(userA);

        byte[] accessKeyBytes = Decoders.BASE64.decode(secretKey);
        TEST_JWT_SECRET_TOKEN_KEY = Keys.hmacShaKeyFor(accessKeyBytes);
    }

    @Test
    @DisplayName("회원가입: 플랫폼 서버(google, kakao)에서 인증 받은 뒤, 추가정보 입력 전 유저 조회")
    void checkUserAlreadyExistsInDb() {
        // email, provider 가 같은 유저가 db에 이미 존재하면, 409 CONFLICT
        int result = authService.checkUserExistsInDb("userA@gmail.com", "google");
        assertThat(result).isEqualTo(HttpStatus.CONFLICT.value());

        // email, provider 가 같은 유저가 db에 없다면, 200 OK
        int result2 = authService.checkUserExistsInDb("userA@gmail.com", "kakao");
        assertThat(result2).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원가입 시에 닉네임 중복 검사")
    void checkNicknameDuplication() {
        // 닉네임 중복 시 CustomException 발생
        int result = authService.checkNicknameDuplication("testA");
        assertThat(result).isEqualTo(HttpStatus.CONFLICT.value());

        // 닉네임 중복이 아닐 시 정상적으로 String 결과 반환
        int result2 = authService.checkNicknameDuplication("testB");
        assertThat(result2).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("정상적으로 회원가입이 동작하는지 확인")
    void signUp() {
        SignUpRequestDto signUpRequestDtoA = createSignUpReqDto("google_123", "tes33t@kakao.com",
                "kakao", "userA");

        int result = authService.signUp(signUpRequestDtoA);

        // db 에 잘 저장되는지 검증
        List<User> userList = userRepository.findAll();
        assertThat(userList.size()).isEqualTo(2);

        assertThat(result).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("회원가입 시 닉네임이 db에 이미 존재하는 경우 CustomException 발생")
    void signUpNicknameDuplicate() {
        // given
        SignUpRequestDto signUpRequestDtoA = createSignUpReqDto("google_123", "test@kakao.com",
                "kakao", "testA");

        assertThrows(CustomException.class, () -> {
            authService.signUp(signUpRequestDtoA);
        });
    }

    /**
     * 로그인
     */
    @Test
    @DisplayName("로그인 시 db에 사용자 정보가 없는 경우 CustomException")
    void signInUserNotExist() {
        // given
        User userB = createUser("kakao_4237", "userA@kakao.com", "kakao", "testA");

        // when
        assertThrows(CustomException.class, () -> {
            authService.signIn(userB.getEmail(), userB.getProvider());
        });
    }

    @Test
    @DisplayName("로그인 시 db에 사용자 정보가 있는 경우 정상 응답")
    void signIn() {
        //ReflectionTestUtils.setField(jwtTokenProvider, "JWT_SECRET_TOKEN_KEY", TEST_JWT_SECRET_TOKEN_KEY);

        // dto 검증
        SignInResponseDto responseDto = authService.signIn("userA@gmail.com", "google");
        assertThat(responseDto.getCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private User createUser(String id, String email, String provider, String nickname) {
        return User.builder()
                .id(id).email(email).provider(provider).nickname(nickname)
                .role(UserRole.MENTEE).birthYear(2000).birthMonth(3)
                .school("고려대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .optionInfo(null)
                .build();
    }

    private SignUpRequestDto createSignUpReqDto(String id, String email, String provider, String nickname) {
        return new SignUpRequestDto(id, email, provider, nickname,
                UserRole.MENTEE, 2000, 3, "고려대학교",
                3, "중반", 2021, 3, "경제학과", null, null,
                "Devops", "AWS", null, null, null, null);
    }

}