package seoultech.capstone.menjil.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    /**
     * Using: AuthService -> generate access, refresh token
     */

    private UserRepository userRepository;
    private SecretKey JWT_SECRET_TOKEN_KEY;
    private static final long accessTokenExpiresIn = Duration.ofMinutes(30).toMillis();    // 만료시간 30분
    private static final long refreshTokenExpiresIn = Duration.ofDays(7).toMillis();    // 만료시간 7일

    public JwtTokenProvider(@Value("${jwt.secret.token}") String tokenKey) {
        byte[] accessKeyBytes = Decoders.BASE64.decode(tokenKey);
        JWT_SECRET_TOKEN_KEY = Keys.hmacShaKeyFor(accessKeyBytes);
        System.out.println("생성자 호출됨");
    }

    // 테스트 코드 작성을 위해, LocalDate 값을 파라미터로 받도록 수정.
    public String generateAccessToken(String userId, LocalDate currentDate) {
        // LocalDate -> Date
        Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .claim("user_id", userId)
                .setSubject("Access_Token")
                .setIssuedAt(date)   // token 발급 시간
                .setExpiration(new Date(date.getTime() + accessTokenExpiresIn))
                .signWith(this.JWT_SECRET_TOKEN_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // 테스트 코드 작성을 위해, LocalDate 값을 파라미터로 받도록 수정.
    public String generateRefreshToken(String userId, LocalDate currentDate) {
        // LocalDate -> Date
        Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .claim("user_id", userId)
                .setSubject("Refresh_Token")
                .setIssuedAt(date)   // token 발급 시간
                .setExpiration(new Date(date.getTime() + refreshTokenExpiresIn))
                .signWith(this.JWT_SECRET_TOKEN_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // access token 검증
    @Transactional
    public boolean validateAccessToken(String accessToken) {
        try {
            log.info(">> validate access token <<");
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_TOKEN_KEY)
                    .build()
                    .parseClaimsJws(accessToken);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // refresh token 검증
    public boolean validateRefreshToken(String refreshToken) {
        try {
            log.info(">> validate refresh token <<");
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_TOKEN_KEY)
                    .build()
                    .parseClaimsJws(refreshToken);

            // 1. Redis 조회해서 refresh token 값이 존재하는지 확인

            // 2. 토큰이 변조되었는지 파악하기 위해, db 에서 user_id 값을 가져와서 조회
            User user = userRepository.findUserById(claims.getBody().get("user_id").toString())
                    .orElse(null);


            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}
