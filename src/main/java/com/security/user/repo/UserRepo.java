package com.security.user.repo;

import com.security.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
     User findByUserName(String username);
    boolean existsByUserNameAndIsActive(String userName , Boolean isActive);

}
