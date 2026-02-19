package com.novelreader.repository;

import com.novelreader.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据昵称查找
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查昵称是否存在
     */
    boolean existsByNickname(String nickname);
}
