package seoultech.capstone.menjil.domain.user.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.user.domain.User;
import seoultech.capstone.menjil.domain.user.domain.UserRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml" +
        ",classpath:/application-security.yml" +
        ",classpath:/application-database.yml" +
        ",classpath:/application-jwt.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Email, Name, Provider 세 가지를 모두 만족하는 User 가 조회되는지를 검증한다.")
    void findUserByEmailAndNameAndProvider() {
        // given
        User userA = createUser("google_1", "userA@gmail.com", "userA", "google", "g1");

        // UserA 와 email, name 동일
        User userB = createUser("google_2", "userA@gmail.com", "userA", "kakao", "g2");

        // UserA 와 email, provider 동일
        User userC = createUser("google_2", "userA@gmail.com", "userB", "google", "g3");

        // UserA 와 name, provider 동일
        User userD = createUser("google_2", "userA@kakao.com", "userB", "google", "g4");

        userRepository.saveAll(List.of(userA, userB, userC, userD));

        // when
        List<User> userList = userRepository.findUserByEmailAndNameAndProvider("userA@gmail.com",
                "userA", "google");

        // then
        assertThat(userList).hasSize(1)
                .extracting("email", "name", "provider")
                .containsExactly(
                        tuple("userA@gmail.com", "userA", "google")
                );
    }

    private User createUser(String id, String email, String name, String provider, String nickname) {
        return User.builder()
                .id(id).email(email).name(name).provider(provider).nickname(nickname)
                .role(UserRole.MENTEE).birthYear(2000).birthMonth(3)
                .school("고려대학교").score(3).scoreRange("중반")
                .graduateDate(2021).major("경제학과").subMajor(null)
                .minor(null).field("백엔드").techStack("AWS")
                .optionInfo(null)
                .build();
    }
}