package seoultech.capstone.menjil.domain.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.user.domain.User;

import java.util.List;
import java.util.Optional;

// CRUD 함수를 JpaRepository 가 가지고 있다.
// JpaRepository 를 상속했으므로 @Repository annotation 이 없어도 IOC 가 된다.
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findUserById(String id);

    List<User> findUserByEmailAndNameAndProvider(String email, String name, String provider);

    List<User> findUserByNickname(String nickname);

    List<User> findAllByNickname(String nickname);

    void deleteUserByNickname(String nickname);
}
