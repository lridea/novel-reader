package com.novelreader.repository;

import com.novelreader.entity.SensitiveWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 敏感词Repository
 */
@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    /**
     * 查询所有启用的敏感词
     */
    List<SensitiveWord> findByEnabledOrderBySeverityDesc(Integer enabled);

    /**
     * 根据单词查询
     */
    Optional<SensitiveWord> findByWord(String word);

    /**
     * 分页查询（按分类和状态）
     */
    Page<SensitiveWord> findByCategoryAndEnabled(String category, Integer enabled, Pageable pageable);

    /**
     * 分页查询（按状态）
     */
    Page<SensitiveWord> findByEnabled(Integer enabled, Pageable pageable);
}
