package seoultech.capstone.menjil.domain.user.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.user.dao.UserRepository;
import seoultech.capstone.menjil.domain.user.domain.User;
import seoultech.capstone.menjil.domain.user.domain.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml" +
        ",classpath:/application-security.yml" +
        ",classpath:/application-database.yml")
@Transactional
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 및 닉네임으로 조회")
    public void save() {
        // given
        User userA = new User("google_test33333333333", "testUserA@gmail.com",
                "testUserAInGoogle", "google", "testUserA입니다", UserRole.MENTEE,
                "1999-04", "경북대학교", 4, "초반", 2023,
                "컴퓨터공학과", null, null, "백엔드", "Spring, AWS", null);
        userRepository.save(userA);

        // when
        User savedUser = userRepository.findUserByNickname("testUserA입니다")
                .orElse(null);

        // then
        assertThat(userA.getNickname()).isEqualTo(savedUser.getNickname());
    }
}