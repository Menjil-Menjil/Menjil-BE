package seoultech.capstone.menjil.domain.auth.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

// CRUD 함수를 JpaRepository 가 가지고 있다.
// JpaRepository 를 상속했으므로 @Repository annotation 이 없어도 IOC 가 된다.
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findUserByEmailAndProvider(String email, String provider);

    Optional<User> findUserByNickname(String nickname);  // Nickname 은 중복이 되지 않으므로, List 가 아닌 Optional 로 조회

    Optional<User> findUserById(String id);

    List<User> findAllByNicknameIn(List<String> nicknames); // 주어진 닉네임 리스트에 해당하는 모든 User 객체를 찾는 메서드

    Page<User> findAll(@NotNull Pageable pageable);
}

