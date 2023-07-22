package seoultech.capstone.menjil.domain.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seoultech.capstone.menjil.domain.auth.domain.RefreshToken;
import seoultech.capstone.menjil.domain.auth.domain.User;

import java.sql.Timestamp;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findRefreshTokenByToken(String token);

    Optional<RefreshToken> findRefreshTokenByUserId(User user);

    void deleteRefreshTokenByUserId(User user);

    @Modifying(clearAutomatically = true)   // 1차캐시와 db 동기화. 그렇지 않으면 test code 에서 오류 발생
    @Query("UPDATE RefreshToken r SET r.token = :token, r.expiryDate = :timestamp WHERE r.userId = :user")
    int updateRefreshToken(@Param("user") User user, @Param("token") String token, @Param("timestamp") Timestamp timestamp);

    @Modifying
    @Query(
            value = "TRUNCATE TABLE refresh_token",
            nativeQuery = true
    )
    void truncateRefreshTokenTable();
}
