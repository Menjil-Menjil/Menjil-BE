package seoultech.capstone.menjil.domain.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignInServiceRequest;
import seoultech.capstone.menjil.domain.auth.application.dto.request.SignUpServiceRequest;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.application.dto.response.SignInResponse;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seoultech.capstone.menjil.global.exception.ErrorIntValue.USER_ALREADY_EXISTED;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.SUCCESS;

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
    void setUp() {
        // @Test , @RepeatedTest , @ParameterizedTest , @TestFactory 가 붙은 테스트 메소드가 실행하기 전에 실행된다.
        // 각 테스트 메서드 전에 실행된다.
        User userA = createUser("google_1", TEST_USER_EMAIL, TEST_USER_PROVIDER, TEST_USER_NICKNAME);
        userRepository.save(userA);
    }

    @Test
    @DisplayName("회원가입: 플랫폼 서버(google, kakao)에서 인증 받은 뒤, 추가정보 입력 전 유저 조회")
    void checkUserExistsInDb_user_already_exists_in_db() {
        // email, provider 가 같은 유저가 db에 이미 존재하는 경우
        int result = authService.findUserInDb(TEST_USER_EMAIL, TEST_USER_PROVIDER);
        assertThat(result).isEqualTo(USER_ALREADY_EXISTED.getValue());

        // email, provider 가 같은 유저가 db에 존재하지 않는 경우
        int result2 = authService.findUserInDb("userA@gmail.com", "kakao");
        assertThat(result2).isEqualTo(SUCCESS.getValue());
    }

    /**
     * findNicknameInDb
     */
    @Test
    @DisplayName("회원가입 시에 닉네임 중복 검사")
    void findNicknameInDb() {
        // 닉네임 중복
        int result = authService.findNicknameInDb(TEST_USER_NICKNAME);
        assertThat(result).isEqualTo(USER_ALREADY_EXISTED.getValue());

        // 닉네임 중복 X
        int result2 = authService.findNicknameInDb("testB");
        assertThat(result2).isEqualTo(SUCCESS.getValue());
    }

    /**
     * signUp
     */
    @Test
    @DisplayName("정상적으로 회원가입이 동작하는지 확인: img_url 세팅 및 데이터 저장")
    void signUp() {
        SignUpServiceRequest request = createSignUpServiceReqDto("google_123", "tes33t@kakao.com",
                "kakao", "userA");

        // @BeforeEach 에서 저장된 데이터 제거
        userRepository.deleteAll();

        authService.signUp(request);
        List<User> userList = userRepository.findAll();
        assertThat(userList.size()).isEqualTo(1);

        // default img url이 잘 저장되는지 검증
        assertThat(userList.get(0).getImgUrl()).isEqualTo(defaultImgUrl);
    }

    @Test
    @DisplayName("회원가입 시 닉네임이 db에 이미 존재하는 경우 CustomException 리턴")
    void signUp_Nickname_already_existed() {
        // given
        SignUpServiceRequest request = createSignUpServiceReqDto("google_123", "test@kakao.com",
                "kakao", TEST_USER_NICKNAME);

        assertThrows(CustomException.class, () -> authService.signUp(request));
    }

    /**
     * signIn
     */
    @Test
    @DisplayName("로그인 시 db에 사용자 정보가 있는 경우 Access Token, Refresh Token, 그 외 사용자 정보를 응답으로 보낸다")
    void signIn() {
        // given
        SignInServiceRequest request = SignInServiceRequest.builder()
                .email(TEST_USER_EMAIL).provider("google").build();

        // when
        SignInResponse response = authService.signIn(request);

        // then
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();

        assertThat(response.getNickname()).isEqualTo(TEST_USER_NICKNAME);
        assertThat(response.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(response.getSchool()).isEqualTo("서울과학기술대학교");
        assertThat(response.getNickname()).isNotBlank();
        assertThat(response.getSchool()).isNotBlank();
        assertThat(response.getMajor()).isNotBlank();
        assertThat(response.getImgUrl()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 시 db에 사용자 정보가 없는 경우 CustomException 리턴")
    void signIn_User_Not_Existed() {
        // given
        SignInServiceRequest request = SignInServiceRequest.builder()
                .email("userA@kakao.com").provider("kakao").build();

        // when // then
        assertThrows(CustomException.class, () -> authService.signIn(request));
    }

    @Test
    @DisplayName("로그인 시 TokenRepository 에 이미 RefreshToken 정보가 있는 경우, Token 값이 update 된다")
    void signIn_update_RefreshToken() throws InterruptedException {
        // given
        SignInServiceRequest request = SignInServiceRequest.builder()
                .email(TEST_USER_EMAIL).provider(TEST_USER_PROVIDER).build();

        // First Login
        SignInResponse responseDto = authService.signIn(request);
        String firstRT = responseDto.getRefreshToken();

        Thread.sleep(3000);

        // Second Login: Update RefreshToken in TokenRepository: firstRT -> secondRT
        SignInResponse responseDto2 = authService.signIn(request);
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
        String major = "컴퓨터공학과";
        String subMajor = null;
        String minor = null;
        String company = null;
        Integer companyYear = 0;
        return User.builder()
                .id(id).email(email).provider(provider).nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("서울과학기술대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major(major).subMajor(subMajor).minor(minor)
                .field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .company(company)
                .companyYear(companyYear)
                .imgUrl(defaultImgUrl)  // set img url
                .build();
    }

    private SignUpServiceRequest createSignUpServiceReqDto(String id, String email, String provider, String nickname) {
        String major = "컴퓨터공학과";
        String subMajor = null;
        String minor = null;
        String company = null;
        Integer companyYear = 3;
        String field = "백엔드";
        String techStack = "AWS";
        return SignUpServiceRequest.builder()
                .userId(id)
                .email(email)
                .provider(provider)
                .nickname(nickname)
                .birthYear(2000)
                .birthMonth(3)
                .school("서울과학기술대학교")
                .score(3)
                .scoreRange("중반")
                .graduateDate(2021)
                .graduateMonth(3)
                .major(major)
                .subMajor(subMajor)
                .minor(minor)
                .field(field)
                .techStack(techStack)
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .company(company)
                .companyYear(companyYear)
                .build();
    }

}