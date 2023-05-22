package seoultech.capstone.menjil.global.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Map;

@Component
public class JwtUtils {
    /**
     * Using:
     * AuthService; OAuthUserResponseDto
     * UserService; signup
     * User validation; JwtExpiredValidator
     */

    private static SecretKey JWT_SECRET_KEY;

    public JwtUtils(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        JWT_SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    public static Map<String, Object> decodeJwt(String jwt) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(getJwtSecretKey())
                .build().parseClaimsJws(jwt);
        return jws.getBody();
    }

    public static SecretKey getJwtSecretKey() {
        return JWT_SECRET_KEY;
    }

    /*public static Key jwtSecretKeyProvider(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }*/

    /*@Value("${jwt.secret}")
    private void setJwtSecretKey(String key) {
        JwtUtils.JWT_SECRET_KEY = key;
    }*/


}
