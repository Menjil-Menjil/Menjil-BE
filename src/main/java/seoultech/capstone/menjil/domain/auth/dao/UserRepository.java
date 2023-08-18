package seoultech.capstone.menjil.domain.auth.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;
import seoultech.capstone.menjil.domain.auth.domain.UserRole;

import java.util.List;
import java.util.Optional;

// CRUD 함수를 JpaRepository 가 가지고 있다.
// JpaRepository 를 상속했으므로 @Repository annotation 이 없어도 IOC 가 된다.
public interface UserRepository extends JpaRepository<User, String> {

    List<User> findUserByEmailAndProvider(String email, String provider);

    Optional<User> findUserByNickname(String nickname);  // Nickname 은 중복이 되지 않으므로, List 가 아닌 Optional 로 조회

    Optional<User> findUserById(String id);

    Page<User> findUsersByRole(UserRole role, Pageable pageable);
}

