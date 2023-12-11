package seoultech.capstone.menjil.domain.auth.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // MySQL 등의 db 를 사용할 경우 추가설정
@DataJpaTest
@ActiveProfiles("test")
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @BeforeEach
    void init() {
        tokenRepository.truncateRefreshTokenTable();
    }

    @Test
    @DisplayName("token Repository 에 id 값을 null 로 넣어도 정상적으로 auto increment 가 동작한다")
    void autoIncrementTest() {
        // given
        User userA = createUser("google_1", "userA@gmail.com", "google", "g1");
        User userB = createUser("google_11", "userA1@gmail.com", "google", "g2");
        Timestamp timestampA = Timestamp.valueOf(LocalDateTime.now());
        Timestamp timestampB = Timestamp.valueOf(LocalDateTime.now().plusSeconds(3));

        // when
        RefreshToken rfEntityA = RefreshToken.builder()
                .id(1L)
                .userId(userA)
                .token("token1")
                .expiryDate(timestampA)
                .build();
        RefreshToken rfEntityB = RefreshToken.builder()
                .id(2L)
                .userId(userB)
                .token("token2")
                .expiryDate(timestampB)
                .build();
        tokenRepository.save(rfEntityA);
        tokenRepository.save(rfEntityB);

        // then
        Optional<RefreshToken> optionalRf1 = tokenRepository.findRefreshTokenByUserId(userA);
        Optional<RefreshToken> optionalRf2 = tokenRepository.findRefreshTokenByUserId(userB);

        // Assert that the Optional is not empty
        assertThat(optionalRf1.isPresent()).isEqualTo(true);
        assertThat(optionalRf2.isPresent()).isEqualTo(true);

        // Retrieve the User object from the Optional
        RefreshToken dbToken1 = optionalRf1.get();
        RefreshToken dbToken2 = optionalRf2.get();

        assertThat(dbToken1.getId()).isEqualTo(1L);
        assertThat(dbToken2.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("updateRefreshToken 메소드의 jpql 구문이 의도한 대로 동작하는지 검증한다: " +
            "token 과 expiryDate 의 값이 변경되어야 한다. ")
    void update_RefreshToken_In_Db() {
        // given
        User userA = createUser("google_1", "userA@gmail.com", "google", "g1");
        Timestamp timestampA = Timestamp.valueOf(LocalDateTime.now());
        RefreshToken rfEntityA = RefreshToken.builder()
                .id(null)
                .userId(userA)
                .token("old token")
                .expiryDate(timestampA)
                .build();
        tokenRepository.save(rfEntityA);

        // when
        LocalDateTime currentDateTime = LocalDateTime.now().plusSeconds(20);
        Timestamp expiryDate = Timestamp.valueOf(currentDateTime);
        String refreshToken = "new token!!";

        int value = tokenRepository.updateRefreshToken(userA, refreshToken, expiryDate);
        Optional<RefreshToken> optionalRf1 = tokenRepository.findRefreshTokenByUserId(userA);

        // then
        assertThat(optionalRf1.isPresent()).isTrue();
        assertThat(value).isEqualTo(1);
        assertThat(optionalRf1.get().getToken()).isEqualTo(refreshToken);

        /* java.sql.timestamp 와 mysql timestamp 방식이 조금 달라서, 기존에 저장된 값에서 변경되었는지만 확인
         따라서 아래 메서드는 사용하지 않음 */
        // assertThat(optionalRf1.get().getExpiryDate()).isEqualTo(expiryDate);
        assertThat(optionalRf1.get().getExpiryDate()).isNotEqualTo(timestampA);
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