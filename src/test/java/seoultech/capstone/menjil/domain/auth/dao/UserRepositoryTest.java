package seoultech.capstone.menjil.domain.auth.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;

@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Email, Provider 두 가지를 모두 만족하는 User 가 조회되는지를 검증한다.")
    void findUserByEmailAndProvider() {

        // given
        User userA = createUser("google_1", "userA@gmail.com", "google", "g1");

        // UserA 와 email 동일
        User userB = createUser("google_2", "userA@gmail.com", "kakao", "g2");

        // UserA 와 provider 동일
        User userC = createUser("google_2", "userAA@gmail.com", "google", "g3");

        userRepository.saveAll(List.of(userA, userB, userC));

        // when
        Optional<User> user = userRepository.findUserByEmailAndProvider("userA@gmail.com", "google");

        // then
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getEmail()).isEqualTo("userA@gmail.com");
        assertThat(user.get().getProvider()).isEqualTo("google");
    }

    @Test
    @DisplayName("Nickname 을 통해 유저가 조회되는지 검증")
    void findUserByNickname() {
        // given
        User userA = createUser("google_1", "userA@gmail.com", "google", "g1");
        User userB = createUser("google_11", "userA1@gmail.com", "google", "g2");
        userRepository.saveAll(List.of(userA, userB));

        // when
        Optional<User> nicknameExistsInDb = userRepository.findUserByNickname("g1");


        // then
        assertThat(nicknameExistsInDb.isPresent()).isTrue();
        assertThat(nicknameExistsInDb.get().getNickname()).isEqualTo("g1");
    }

    @Test
    @DisplayName("중복된 닉네임이 db에 여러 개인 경우, 모두 불러오는지 검증")
    void findAllByNickname() {
        // given
        User userA = createUser("google_1", "userA@gmail.com", "google", "g1");
        User userB = createUser("google_11", "userA1@gmail.com", "google", "g2");


        // when


        // then

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
}