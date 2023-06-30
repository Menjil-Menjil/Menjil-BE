package seoultech.capstone.menjil.domain.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.auth.domain.User;

import java.util.List;

// CRUD 함수를 JpaRepository 가 가지고 있다.
// JpaRepository 를 상속했으므로 @Repository annotation 이 없어도 IOC 가 된다.
public interface UserRepository extends JpaRepository<User, String> {

    List<User> findUserByEmailAndProvider(String email, String provider);

    List<User> findUserByNickname(String nickname);

}

