package seoultech.capstone.menjil.domain.auth.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.dto.request.SignUpRequest;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceExceptionTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void signUp_Should_Throw_CustomException_WhenSaveFails() {
        // Arrange
        SignUpRequest SignUpRequestA = createSignUpReqDto("google_123", "tes33t@kakao.com",
                "kakao", "userA");

        // DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

        // Act and Assert
        assertThrows(CustomException.class, () -> authService.signUp(SignUpRequestA));
    }

    @Test
    void signIn_Should_Throw_CustomException_When_Token_SaveFails() {
        // Arrange
        String testUserId = "google_1234123455";
        String testEmail = "testAA@google.com";
        String testProvider = "google";

        User mockUser = createUser(testUserId, testEmail, testProvider, "testUserAA");
        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";

        when(userRepository.findUserByEmailAndProvider(testEmail, testProvider))
                .thenReturn(Optional.ofNullable(mockUser));
        when(jwtTokenProvider.generateAccessToken(anyString(), any(LocalDateTime.class))).thenReturn(mockAccessToken);
        when(jwtTokenProvider.generateRefreshToken(anyString(), any(LocalDateTime.class))).thenReturn(mockRefreshToken);
        when(tokenRepository.findRefreshTokenByUserId(mockUser)).thenReturn(Optional.empty());

        // DataIntegrityViolationException
        doThrow(DataIntegrityViolationException.class).when(tokenRepository).save(any(RefreshToken.class));

        // Act and Assert
        assertThrows(CustomException.class, () -> authService.signIn(testEmail, testProvider));
    }


    private SignUpRequest createSignUpReqDto(String id, String email, String provider, String nickname) {
        Integer birthYear = 2000;
        Integer birthMonth = 3;
        String school = "고려대학교";
        Integer score = 3;
        String scoreRange = "중반";
        Integer graduateDate = 2021;
        Integer graduateMonth = 3;
        String major = "컴퓨터공학과";
        String subMajor = null;
        String minor = null;
        String company = null;
        Integer companyYear = 3;
        String field = "백엔드";
        String techStack = "AWS";
        return new SignUpRequest(id, email, provider, nickname,
                birthYear, birthMonth, school, score, scoreRange,
                graduateDate, graduateMonth, major, subMajor, minor,
                company, companyYear, field, techStack, null, null, null, null);
    }

    private User createUser(String id, String email, String provider, String nickname) {
        return User.builder()
                .id(id).email(email).provider(provider).nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("고려대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .build();
    }

}
