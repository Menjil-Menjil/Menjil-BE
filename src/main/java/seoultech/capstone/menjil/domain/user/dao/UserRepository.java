package seoultech.capstone.menjil.domain.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.user.domain.User;

import java.util.Optional;

// CRUD 함수를 JpaRepository 가 가지고 있다.
// JpaRepository 를 상속했으므로 @Repository annotation 이 없어도 IOC 가 된다.
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);

    void deleteByProviderId(User user);
}
