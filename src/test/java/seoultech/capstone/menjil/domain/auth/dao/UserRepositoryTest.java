package seoultech.capstone.menjil.domain.auth.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import seoultech.capstone.menjil.domain.auth.domain.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // To use MySQL
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Email, Provider 두 가지를 모두 만족하는 User가 조회되는지를 검증한다.")
    void findUserByEmailAndProvider() {

        // given
        String email = "userA@gmail.com";
        String provider = "google";
        User userA = createTestUser("google_1", email, provider, "g1");

        // UserA 와 email 동일
        User userB = createTestUser("google_2", "userA@gmail.com", "kakao", "g2");

        // UserA 와 provider 동일
        User userC = createTestUser("google_2", "userAA@gmail.com", "google", "g3");

        userRepository.saveAll(List.of(userA, userB, userC));

        // when
        Optional<User> user = userRepository.findUserByEmailAndProvider(email, provider);

        // then
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getEmail()).isEqualTo(email);
        assertThat(user.get().getProvider()).isEqualTo(provider);
    }

    @Test
    @DisplayName("Nickname을 통해 유저가 조회되는지 검증")
    void findUserByNickname() {
        // given
        String email = "userA@gmail.com";
        String nickname = "g1";
        User userA = createTestUser("google_1", email, "google", nickname);
        User userB = createTestUser("google_11", "userA1@gmail.com", "google", "g2");
        userRepository.saveAll(List.of(userA, userB));

        // when
        Optional<User> nicknameExistsInDb = userRepository.findUserByNickname(nickname);

        // then
        assertThat(nicknameExistsInDb.isPresent()).isTrue();
        assertThat(nicknameExistsInDb.get().getNickname()).isEqualTo(nickname);
        assertThat(nicknameExistsInDb.get().getEmail()).isEqualTo(email);
        assertThat(nicknameExistsInDb.get().getCareer()).isNull();  // NULL 검증
    }


    private User createTestUser(String id, String email, String provider, String nickname) {
        return User.builder()
                .id(id).email(email).provider(provider).nickname(nickname)
                .birthYear(2000).birthMonth(3)
                .school("고려대학교").score(3).scoreRange("중반")
                .graduateDate(2021).graduateMonth(3)
                .major("경제학과").subMajor(null).minor(null)
                .company(null).companyYear(null)
                .field("백엔드").techStack("AWS")
                .career(null)
                .certificate(null)
                .awards(null)
                .activity(null)
                .build();
    }
}