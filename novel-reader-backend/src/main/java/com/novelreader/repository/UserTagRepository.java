package com.novelreader.repository;

import com.novelreader.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户标签Repository
 */
@Repository
public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    /**
     * 根据用户、书籍、标签查询
     */
    Optional<UserTag> findByUserIdAndNovelIdAndTag(Long userId, Long novelId, String tag);

    /**
     * 检查用户、书籍、标签是否存在
     */
    boolean existsByUserIdAndNovelIdAndTag(Long userId, Long novelId, String tag);

    /**
     * 删除用户标签
     */
    void deleteByUserIdAndNovelIdAndTag(Long userId, Long novelId, String tag);

    /**
     * 查询书籍的所有用户标签
     */
    @Query("SELECT DISTINCT ut.tag FROM UserTag ut WHERE ut.novelId = :novelId")
    List<String> findTagsByNovelId(@Param("novelId") Long novelId);

    /**
     * 统计书籍的用户标签数量
     */
    long countByNovelId(Long novelId);

    /**
     * 查询所有不同的标签
     */
    @Query("SELECT DISTINCT ut.tag FROM UserTag ut")
    List<String> findAllTags();
}
