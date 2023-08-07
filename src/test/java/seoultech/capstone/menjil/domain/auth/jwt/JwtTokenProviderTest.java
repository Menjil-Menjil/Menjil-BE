package seoultech.capstone.menjil.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.secret.token}")
    private String secretKey;
    private SecretKey TEST_JWT_SECRET_TOKEN_KEY;
    private final String USER_ID = "google_8824312";

    @BeforeEach
    public void init() {
        byte[] accessKeyBytes = Decoders.BASE64.decode(secretKey);
        TEST_JWT_SECRET_TOKEN_KEY = Keys.hmacShaKeyFor(accessKeyBytes);
    }

    @Test
    @DisplayName("Access Token, Refresh Token 이 생성된다")
    void generateTokens() {
        //ReflectionTestUtils.setField(jwtTokenProvider, "JWT_SECRET_TOKEN_KEY", TEST_JWT_SECRET_TOKEN_KEY);
        String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, LocalDateTime.now());
        String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, LocalDateTime.now());

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();

        /* Access Token 에, user_id 가 정상적으로 들어갔는지 검증 */
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(this.TEST_JWT_SECRET_TOKEN_KEY)
                .build()
                .parseClaimsJws(accessToken);
        String userIdInAccessToken = claims.getBody().get("user_id").toString();
        assertThat(userIdInAccessToken).isEqualTo(USER_ID);

        /* RefreshToken 에, user_id 가 정상적으로 들어갔는지 검증 */
        Jws<Claims> claims2 = Jwts.parserBuilder()
                .setSigningKey(this.TEST_JWT_SECRET_TOKEN_KEY)
                .build()
                .parseClaimsJws(accessToken);
        String userIdInRefreshToken = claims2.getBody().get("user_id").toString();
        assertThat(userIdInRefreshToken).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Access Token 이 만료시간이 지났을 시 TokenStatus.Expired 값을 반환하는지 확인")
    void accessToken_is_expired() throws InterruptedException {
        // Set the token expiration time to 1 second
        int expirationSeconds = 1;

        // Get the current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Add the expiration time to the current time
        LocalDateTime expirationTime = currentTime.plusSeconds(expirationSeconds);

        String token = Jwts.builder()
                .claim("user_id", USER_ID)
                .setSubject("Access_Token")
                .setIssuedAt(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))   // token 발급 시간
                .setExpiration(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(TEST_JWT_SECRET_TOKEN_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Sleep for more than the token expiration time
        Thread.sleep((expirationSeconds + 1) * 1000);

        assertThat(jwtTokenProvider.validateAccessToken(token)).isEqualTo(TokenStatus.EXPIRED);
    }

    @Test
    @DisplayName("Access Token 의 user_id가 db에 없을 시, TokenStatus.USER_ID_NOT_EXIST 값을 반환하는지 확인")
    void test() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, LocalDateTime.now());

        // db 에 저장하는 로직이 없기 때문에, 따로 db 에 저장되지 않음.

        // then
        assertThat(jwtTokenProvider.validateAccessToken(accessToken)).isEqualTo(TokenStatus.USER_ID_NOT_EXIST);
    }

}