package seoultech.capstone.menjil.domain.user.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
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
import seoultech.capstone.menjil.domain.user.dto.request.UserRequestDto;
import seoultech.capstone.menjil.global.exception.CustomException;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static seoultech.capstone.menjil.global.common.JwtUtils.getJwtSecretKey;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml" +
        ",classpath:/application-security.yml" +
        ",classpath:/application-database.yml")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String jwtDataA, jwtDataB;

    @BeforeEach
    public void initGenerateJwtData() {
        Key key = getJwtSecretKey();
        Date now = new Date();
        long expireTime = Duration.ofDays(360).toMillis();    // 만료날짜 360일 이후.

        // Set header
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS512");

        // Set payload A
        Map<String, Object> payloadA = new HashMap<>();
        payloadA.put("id", "test_A");
        payloadA.put("email", "userServiceTestA@gmail.com");
        payloadA.put("name", "testJwtUserA");
        payloadA.put("provider", "google");
        jwtDataA = Jwts.builder()
                .setHeader(header)
                .setClaims(payloadA)
                .setSubject("UserServiceTest")
                .setIssuedAt(now)   // token 발급 시간
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // Set payload B
        Map<String, Object> payloadB = new HashMap<>();
        payloadB.put("id", "test_B");
        payloadB.put("email", "userServiceTestB@gmail.com");
        payloadB.put("name", "testJwtUserB");
        payloadB.put("provider", "google");
        jwtDataB = Jwts.builder()
                .setHeader(header)
                .setClaims(payloadB)
                .setSubject("UserServiceTest")
                .setIssuedAt(now)   // token 발급 시간
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 가입 검증은 UserService 에서 하므로, UserService 의 signUp 메소드를 검증한다.
     * 하지만 UserService.signup()은 userRequestDto 를 파라미터로 받는다.
     * 따라서, 토큰 유효 기간이 매우 긴 jwtData 를 위에서 생성한 뒤, 사용함
     */
    @Test
    @DisplayName("정상 회원 가입")
    public void signUp() {
        // given
        UserRequestDto userRequestDtoA = new UserRequestDto(jwtDataA, "testUserA", UserRole.MENTEE,
                1999, 3, "경북대학교", 4, "초반", 2023,
                "컴퓨터공학과", null, null, "백엔드", "Spring, AWS", null, null, null, null);

        userService.signUp(userRequestDtoA);

        // when
        User userA = userRepository.findUserByNickname("testUserA").orElse(null);

        // then
        // jwt 토큰이 정상적으로 decode 되어서 db 에 들어갔는지 확인
        assert userA != null;
        assertThat(userA.getId()).isEqualTo("test_A");
        assertThat(userA.getEmail()).isEqualTo("userServiceTestA@gmail.com");
        assertThat(userA.getName()).isEqualTo("testJwtUserA");
        assertThat(userA.getProvider()).isEqualTo("google");

        // 토큰 이외에 기타 정보들이 잘 들어갔는지 확인
        assertThat(userA.getNickname()).isEqualTo("testUserA");
        assertThat(userA.getSchool()).isEqualTo("경북대학교");
    }

    @Test
    @DisplayName("닉네임 중복 가입 요청 시 CustomException")
    public void duplicateUser() {
        // given
        UserRequestDto userRequestDtoA = new UserRequestDto(jwtDataA, "testUserA", UserRole.MENTEE,
                1999, 3, "경북대학교", 4, "초반", 2023,
                "컴퓨터공학과", null, null, "백엔드", "Spring, AWS", null, null, null, null);

        UserRequestDto userRequestDtoB = new UserRequestDto(jwtDataB, "testUserA", UserRole.MENTEE,
                1995, 9, "영남대학교", 3, "중반", 2022,
                "컴퓨터공학과", null, null, "DevOps", "Docker", null, null, null, null);

        // when
        userService.signUp(userRequestDtoA);

        // then
        assertThatThrownBy(() -> userService.signUp(userRequestDtoB))
                .isInstanceOf(CustomException.class);

        // 예외 처리 이후 실제로 db에 저장되지 않았는지 검증
        List<User> testUserAList = userRepository.findAllByNickname("testUserA");
        assertThat(testUserAList.size()).isEqualTo(1);
    }


    /**
     * 해당 부분은 Repository 코드 기본 검증이라 굳이 필요할까 싶은 test 부분이다.
     */
    @Test
    @DisplayName("회원가입 및 닉네임으로 조회")
    public void save() {
        // given
        User userA = new User("google_test33333333333", "testUserA@gmail.com",
                "testUserAInGoogle", "google", "testUserA입니다", UserRole.MENTEE,
                1999, 3, "경북대학교", 4, "초반", 2023,
                "컴퓨터공학과", null, null, "백엔드", "Spring, AWS", null);
        userRepository.save(userA);

        // when
        User savedUser = userRepository.findUserByNickname("testUserA입니다")
                .orElse(null);

        // then
        assert savedUser != null;
        assertThat(userA.getNickname()).isEqualTo(savedUser.getNickname());
    }
}