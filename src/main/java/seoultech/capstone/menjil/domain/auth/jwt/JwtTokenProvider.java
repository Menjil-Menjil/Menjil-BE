package seoultech.capstone.menjil.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seoultech.capstone.menjil.domain.auth.dao.TokenRepository;
import seoultech.capstone.menjil.domain.auth.dao.UserRepository;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Transactional
@Component
public class JwtTokenProvider {
    /**
     * Using: AuthService -> generate Access Token, Refresh Token
     */
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SecretKey JWT_SECRET_TOKEN_KEY;
    private static final long accessTokenExpiresIn = Duration.ofMinutes(3).toMillis();    // 만료시간 1시간 -> 3분 수정
    private static final long refreshTokenExpiresIn = 14L;    // 만료시간 14일

    public JwtTokenProvider(@Value("${jwt.secret.token}") String tokenKey,
                            UserRepository userRepository, TokenRepository tokenRepository) {
        byte[] accessKeyBytes = Decoders.BASE64.decode(tokenKey);
        JWT_SECRET_TOKEN_KEY = Keys.hmacShaKeyFor(accessKeyBytes);
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    // 테스트 코드 작성을 위해, LocalDateTime 값을 파라미터로 받도록 수정.
    public String generateAccessToken(String userId, LocalDateTime currentDateTime) {
        // LocalDateTime -> Date
        ZoneId zoneId = ZoneId.systemDefault(); // Get the system default time zone
        Date date = Date.from(currentDateTime.atZone(zoneId).toInstant()); // Convert LocalDateTime to Date
        return Jwts.builder()
                .claim("user_id", userId)
                .setSubject("Access_Token")
                .setIssuedAt(date)   // token 발급 시간
                .setExpiration(new Date(date.getTime() + accessTokenExpiresIn))
                .signWith(this.JWT_SECRET_TOKEN_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // 테스트 코드 작성을 위해, LocalDate 값을 파라미터로 받도록 수정.
    public String generateRefreshToken(String userId, LocalDateTime currentDateTime) {
        // LocalDateTime -> Date
        ZoneId zoneId = ZoneId.systemDefault(); // Get the system default time zone
        Date date = Date.from(currentDateTime.atZone(zoneId).toInstant()); // Convert LocalDateTime to Date

        return Jwts.builder()
                .claim("user_id", userId)
                .setSubject("Refresh_Token")
                .setIssuedAt(date)   // token 발급 시간
                .setExpiration(new Date(date.getTime() + refreshTokenExpiresIn))
                .signWith(this.JWT_SECRET_TOKEN_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /* Access Token 검증 */
    @Transactional
    public TokenStatus validateAccessToken(String accessToken) {
        try {
            log.info(">> validate Access Token <<");
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_TOKEN_KEY)
                    .build()
                    .parseClaimsJws(accessToken);

            /* case 2: Check if Access Token has expired */
            String userIdInToken = claims.getBody().get("user_id").toString();
            User user = userRepository.findUserById(userIdInToken)
                    .orElse(null);
            if (user == null) {
                return TokenStatus.USER_ID_NOT_EXIST;
            }

            /* case 3 : Other Exception */
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            //log.info(">> Invalid JWT Access Token", e);
            return TokenStatus.OTHER_EXCEPTION;
        }
        /* case 1: Check if Access Token has expired */ catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            //log.info(">> Unsupported JWT Access Token", e);
            return TokenStatus.OTHER_EXCEPTION;
        } catch (IllegalArgumentException e) {
            // log.info(">> JWT Access Token claims string is empty.", e);
            return TokenStatus.OTHER_EXCEPTION;
        }
        return TokenStatus.RELIABLE;
    }

    /* Refresh Token 검증 */
    @Transactional
    public TokenStatus validateRefreshToken(String refreshToken) {
        try {
            log.info(">> validate Refresh Token <<");
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_TOKEN_KEY)
                    .build()
                    .parseClaimsJws(refreshToken);

            /* case 1: db 조회해서 Refresh Token 값이 존재하는지 확인
             존재하지 않는다면, 바로 로그아웃 처리 */
            Optional<RefreshToken> findTokenInDb = tokenRepository.findRefreshTokenByToken(refreshToken);
            if (findTokenInDb.isEmpty()) {
                return TokenStatus.REFRESH_TOKEN_NOT_EXIST;
            }

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            //log.info(">> Invalid JWT Refresh Token", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 유효하지 않은 JWT Refresh Token입니다");
            return TokenStatus.OTHER_EXCEPTION;
        }
        /* case 3: AWS Lambda 로 처리하겠지만, 토큰 유효 기간이 만료된 경우 검증 */ catch (ExpiredJwtException e) {
            //result.put("message", "Access Token이 만료되었습니다. 그리고 만료된 JWT Refresh Token입니다");
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            //log.info(">> Unsupported JWT Refresh Token", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 지원되지 않는 JWT Refresh Token입니다");
            return TokenStatus.OTHER_EXCEPTION;
        } catch (IllegalArgumentException e) {
            //log.info(">> JWT Refresh Token claims string is empty", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 Refresh Token의 payload 혹은 signature 값이 올바르지 않습니다");
            return TokenStatus.OTHER_EXCEPTION;
        }
        return TokenStatus.RELIABLE;
    }

}
