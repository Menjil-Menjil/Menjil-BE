package seoultech.capstone.menjil.domain.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequest;
import seoultech.capstone.menjil.domain.auth.dto.response.SignInResponse;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;

    private final String TEST_USER_EMAIL = "testUserA@gmail.com";
    private final String TEST_USER_NICKNAME = "testUserA";
    private final String TEST_USER_PROVIDER = "google";
    private static final String defaultImgUrl = "profile/default.png";

    @BeforeEach
    void init() {
        // @Test , @RepeatedTest , @ParameterizedTest , @TestFactory 가 붙은 테스트 메소드가 실행하기 전에 실행된다.
        // 각 테스트 메서드 전에 실행된다.
        User userA = createUser("google_1", TEST_USER_EMAIL, TEST_USER_PROVIDER, TEST_USER_NICKNAME);
        userRepository.save(userA);
    }

    @Test
    @DisplayName("회원가입: 플랫폼 서버(google, kakao)에서 인증 받은 뒤, 추가정보 입력 전 유저 조회")
    void checkUserExistsInDb_user_already_exists_in_db() {
        // email, provider 가 같은 유저가 db에 이미 존재하면, 409 CONFLICT
        int result = authService.findUserInDb(TEST_USER_EMAIL, TEST_USER_PROVIDER);
        assertThat(result).isEqualTo(HttpStatus.CONFLICT.value());

        // email, provider 가 같은 유저가 db에 없다면, 200 OK
        int result2 = authService.findUserInDb("userA@gmail.com", "kakao");
        assertThat(result2).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원가입 시에 닉네임 중복 검사")
    void checkNicknameDuplication() {
        // 닉네임 중복 시 CustomException 발생
        int result = authService.findNicknameInDb(TEST_USER_NICKNAME);
        assertThat(result).isEqualTo(HttpStatus.CONFLICT.value());

        // 닉네임 중복이 아닐 시 정상적으로 String 결과 반환
        int result2 = authService.findNicknameInDb("testB");
        assertThat(result2).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * signUp()
     */
    @Test
    @DisplayName("정상적으로 회원가입이 동작하는지 확인: img_url 세팅 및 데이터 저장")
    void signUp() {
        SignUpRequest SignUpRequestA = createSignUpReqDto("google_123", "tes33t@kakao.com",
                "kakao", "userA");

        // @BeforeEach 에서 저장된 데이터 제거
        userRepository.deleteAll();

        int result = authService.signUp(SignUpRequestA);
        List<User> userList = userRepository.findAll();
        assertThat(userList.size()).isEqualTo(1);

        // default img url이 잘 저장되는지 검증
        assertThat(userList.get(0).getImgUrl()).isEqualTo(defaultImgUrl);

        assertThat(result).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("회원가입 시 닉네임이 db에 이미 존재하는 경우 CustomException 리턴")
    void signUp_Nickname_already_existed() {
        // given
        SignUpRequest SignUpRequestA = createSignUpReqDto("google_123", "test@kakao.com",
                "kakao", TEST_USER_NICKNAME);

        assertThrows(CustomException.class, () -> authService.signUp(SignUpRequestA));
    }

    /**
     * signIn()
     */
    @Test
    @DisplayName("로그인 시 db에 사용자 정보가 있는 경우 Access Token, Refresh Token, 그 외 사용자 정보를 응답으로 보낸다")
    void signIn() {
        // dto 검증
        SignInResponse responseDto = authService.signIn(TEST_USER_EMAIL, "google");

        assertThat(responseDto.getAccessToken()).isNotNull();
        assertThat(responseDto.getRefreshToken()).isNotNull();

        assertThat(responseDto.getNickname()).isEqualTo(TEST_USER_NICKNAME);
        assertThat(responseDto.getMajor()).isEqualTo("경제학과");
        assertThat(responseDto.getSchool()).isEqualTo("서울과학기술대학교");
        assertThat(responseDto.getNickname()).isNotBlank();
        assertThat(responseDto.getSchool()).isNotBlank();
        assertThat(responseDto.getMajor()).isNotBlank();
        assertThat(responseDto.getImgUrl()).isNotBlank();

    }

    @Test
    @DisplayName("로그인 시 db에 사용자 정보가 없는 경우 CustomException 리턴")
    void signIn_User_Not_Existed() {
        // given
        User userB = createUser("kakao_4237", "userA@kakao.com", "kakao", "testA");

        // when
        assertThrows(CustomException.class, () -> authService.signIn(userB.getEmail(), userB.getProvider()));
    }

    @Test
    @DisplayName("로그인 시 TokenRepository 에 이미 RefreshToken 정보가 있는 경우, Token 값이 update 된다")
    void signIn_update_RefreshToken() throws InterruptedException {
        // First Login
        SignInResponse responseDto = authService.signIn(TEST_USER_EMAIL, TEST_USER_PROVIDER);
        String firstRT = responseDto.getRefreshToken();

        Thread.sleep(3000);

        // Second Login: Update RefreshToken in TokenRepository: firstRT -> secondRT
        SignInResponse responseDto2 = authService.signIn(TEST_USER_EMAIL, TEST_USER_PROVIDER);
        String secondRT = responseDto2.getRefreshToken();
        assertThat(firstRT).isNotEqualTo(secondRT);

        // Not Exists: Because of Update
        RefreshToken findFirstRt = tokenRepository.findRefreshTokenByToken(firstRT)
                .orElse(null);
        assertThat(findFirstRt).isNull();

        // Exists
        RefreshToken findSecondRt = tokenRepository.findRefreshTokenByToken(secondRT)
                .orElse(null);
        assertThat(findSecondRt).isNotNull();
    }

    private User createUser(String id, String email, String provider, String nickname) {
        return User.builder()
                .id(id).email(email).provider(provider).nickname(nickname)
                .role(UserRole.MENTEE).birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .optionInfo(null)
                .imgUrl(defaultImgUrl)  // set img url
                .build();
    }

    private SignUpRequest createSignUpReqDto(String id, String email, String provider, String nickname) {
        return new SignUpRequest(id, email, provider, nickname,
                UserRole.MENTEE, 2000, 3, "고려대학교",
                3, "중반", 2021, 3, "경제학과", null, null, null,
                "Devops", "AWS", null, null, null, null);
    }

}