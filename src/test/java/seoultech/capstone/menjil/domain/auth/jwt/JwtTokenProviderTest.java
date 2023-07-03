package seoultech.capstone.menjil.domain.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml" +
        ",classpath:/application-database-test.yml" +
        ",classpath:/application-jwt.properties")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.secret.token}")
    private String secretKey;
    private SecretKey TEST_JWT_SECRET_TOKEN_KEY;
    private final String userId = "google_8824312";

    @BeforeEach
    public void init() {
        byte[] accessKeyBytes = Decoders.BASE64.decode(secretKey);
        TEST_JWT_SECRET_TOKEN_KEY = Keys.hmacShaKeyFor(accessKeyBytes);
    }

    @Test
    @DisplayName("access token, refresh token 이 올바르게 생성된다")
    void generateTokens() {
        ReflectionTestUtils.setField(jwtTokenProvider, "JWT_SECRET_TOKEN_KEY", TEST_JWT_SECRET_TOKEN_KEY);

        String accessToken = jwtTokenProvider.generateAccessToken(userId, LocalDate.now());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, LocalDate.now());

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
    }

    @Test
    @DisplayName("access token이 만료시간이 지났을 시 false 값을 반환하는지 확인")
    void validateToken() throws InterruptedException {
        // Set the token expiration time to 1 second
        int expirationSeconds = 1;

        // Get the current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Add the expiration time to the current time
        LocalDateTime expirationTime = currentTime.plusSeconds(expirationSeconds);

        String token = Jwts.builder()
                .claim("user_id", userId)
                .setSubject("Access_Token")
                .setIssuedAt(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))   // token 발급 시간
                .setExpiration(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(TEST_JWT_SECRET_TOKEN_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Sleep for more than the token expiration time
        Thread.sleep((expirationSeconds + 1) * 1000);

        // when
//        assertThrows(ExpiredJwtException.class, () -> {
//            jwtTokenProvider.validateAccessToken(token);
//        });
        assertThat(jwtTokenProvider.validateAccessToken(token)).isEqualTo(false);
    }
}