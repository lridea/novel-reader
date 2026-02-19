package com.novelreader.repository;

import com.novelreader.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 标签 Repository
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 根据名称查找
     */
    Optional<Tag> findByName(String name);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 获取所有标签
     */
    @Query("SELECT t.name as name FROM Tag t ORDER BY t.createdAt DESC")
    List<Map<String, Object>> findAllTags();

    /**
     * 根据来源获取标签
     */
    @Query("SELECT t.name as name FROM Tag t WHERE t.source = :source ORDER BY t.createdAt DESC")
    List<Map<String, Object>> findBySource(String source);
}
