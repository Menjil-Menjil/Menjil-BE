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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Transactional
@Component
public class JwtTokenProvider {
    /**
     * Using: AuthService -> generate access, refresh token
     */
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SecretKey JWT_SECRET_TOKEN_KEY;
    private static final long accessTokenExpiresIn = Duration.ofMinutes(60).toMillis();    // 만료시간 1시간
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

    // access token 검증
    @Transactional
    public boolean validateAccessToken(String accessToken) {
        try {
            log.info(">> validate access token <<");
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_TOKEN_KEY)
                    .build()
                    .parseClaimsJws(accessToken);

            return true;
            //return !claims.getBody().getExpiration().before(new Date());
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            //log.info(">> Invalid JWT Access Token", e);
        } catch (ExpiredJwtException e) {
            //log.info(">> Expired JWT Access Token", e);
        } catch (UnsupportedJwtException e) {
            //log.info(">> Unsupported JWT Access Token", e);
        } catch (IllegalArgumentException e) {
            // log.info(">> JWT Access Token claims string is empty.", e);
        }
        return false;
    }

    // refresh token 검증
    public ConcurrentHashMap<String, Object> validateRefreshToken(String refreshToken) {

        ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<>();
        result.put("tokenStatus", false);
        result.put("refreshStatus", false); // 갱신 필요한 여부. 만료 일자 4일 이내인 경우, 값을 true 로 설정.
        result.put("accessToken", "");
        result.put("refreshToken", "");
        result.put("message", "");

        try {
            log.info(">> validate refresh token <<");
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(this.JWT_SECRET_TOKEN_KEY)
                    .build()
                    .parseClaimsJws(refreshToken);

            /* 1. db 조회해서 refresh token 값이 존재하는지 확인(최우선)
             존재하지 않는다면, 2, 3번을 수행할 필요없이 바로 로그아웃 처리 */
            Optional<RefreshToken> findTokenInDb = tokenRepository.findRefreshTokenByToken(refreshToken);
            if (findTokenInDb.isEmpty()) {
                result.put("status", 401);  // 권한 없음
                result.put("message", "권한이 없습니다. 재로그인 해 주세요");
                return result;
            }

            /* 2. 토큰이 변조되었는지 파악하기 위해, db 에서 user_id 값을 가져와서 조회 */
            String userIdInToken = claims.getBody().get("user_id").toString();
            User user = userRepository.findUserById(userIdInToken)
                    .orElse(null);

            if (user == null) {
                result.put("status", 401);  // 권한 없음
                result.put("message", "권한이 없습니다. 재로그인 해 주세요");
            } else {
                // 토큰이 변조되지 않은 경우
                // 3. refresh token 의 만료 일자가 얼마 남았는지 확인
                // 만료 일자가 4일 이내라면, refresh token 도 새로 발급하도록
                // 만료된 토큰의 경우 Body 를 조회할 수 없다: 이 경우 1번에서 체크하므로 여기서는 체크할 필요 없음.
                long expirationTimeMillis = claims.getBody().getExpiration().getTime(); // 토큰의 만료 시간(ms)
                long currentTimeMillis = System.currentTimeMillis(); // 현재 시간(ms)
                long remainingTimeMillis = expirationTimeMillis - currentTimeMillis; // 남은 유효 시간(ms)
                long remainingTimeDays = ((((remainingTimeMillis / 1000) / 60) / 60) / 24); // 남은 유효 시간(일)

                String newAccessToken = generateAccessToken(userIdInToken, LocalDateTime.now());

                if (remainingTimeDays <= 4) {
                    // access, refresh token 새로 발급
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String newRefreshToken = generateRefreshToken(userIdInToken, currentDateTime);
                    result.put("tokenStatus", true);
                    result.put("refreshStatus", true); // 갱신 필요한 여부. 만료 일자 4일 이내인 경우, 값을 true 로 설정.
                    result.put("accessToken", newAccessToken);
                    result.put("refreshToken", newRefreshToken);
                    result.put("status", 201);
                    result.put("message", "Access Token, Refresh Token 모두 재발급");

                    Timestamp expiryDate = Timestamp.valueOf(currentDateTime.plusDays(14)); // 만료 날짜는 +14일

                    // db 접근해서 Refresh Token 만료 일자 update
                    int updDb = tokenRepository.updateRefreshToken(user, newRefreshToken,
                            expiryDate);

                } else {
                    // access token 만 새로 발급
                    result.put("tokenStatus", true);
                    result.put("accessToken", newAccessToken);
                    result.put("refreshToken", refreshToken);
                    result.put("status", 201);
                    result.put("message", "Access Token만 재발급");
                }
            }
            return result;
            //return !claims.getBody().getExpiration().before(new Date());

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            //log.info(">> Invalid JWT Refresh Token", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 유효하지 않은 JWT Refresh Token입니다");
        } catch (ExpiredJwtException e) {
            //log.info(">> Expired JWT Refresh Token", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 만료된 JWT Refresh Token입니다");
        } catch (UnsupportedJwtException e) {
            //log.info(">> Unsupported JWT Refresh Token", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 지원되지 않는 JWT Refresh Token입니다");
        } catch (IllegalArgumentException e) {
            //log.info(">> JWT Refresh Token claims string is empty", e);
            //result.put("message", "Access Token이 만료되었습니다. 그리고 Refresh Token의 payload 혹은 signature 값이 올바르지 않습니다");
        }
        result.put("status", 401);
        result.put("message", "Access Token, Refresh Token 모두 유효하지 않습니다. 재로그인 해 주세요");
        return result;
    }

}
