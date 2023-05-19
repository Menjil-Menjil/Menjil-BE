package seoultech.capstone.menjil.global.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Map;

@Component
public class JwtUtils {

    private static String JWT_SECRET_KEY;

    public static Key jwtSecretKeyProvider(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static Map<String, Object> decodeJwt(String jwt) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKeyProvider(JWT_SECRET_KEY))
                .build().parseClaimsJws(jwt);
        return jws.getBody();
    }

    @Value("${jwt.secret}")
    private void setJwtSecretKey(String key) {
        JwtUtils.JWT_SECRET_KEY = key;
    }
}
