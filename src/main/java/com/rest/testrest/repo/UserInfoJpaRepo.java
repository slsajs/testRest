package com.rest.testrest.repo;

import com.rest.testrest.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoJpaRepo  extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByUserId(String userId);

}
