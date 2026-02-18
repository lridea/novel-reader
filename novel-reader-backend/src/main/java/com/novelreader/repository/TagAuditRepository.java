package com.novelreader.repository;

import com.novelreader.entity.TagAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签审核Repository
 */
@Repository
public interface TagAuditRepository extends JpaRepository<TagAudit, Long> {

    /**
     * 根据用户、书籍、标签查询审核记录
     */
    Optional<TagAudit> findByUserIdAndNovelIdAndTag(Long userId, Long novelId, String tag);

    /**
     * 检查用户、书籍、标签的审核记录是否存在
     */
    boolean existsByUserIdAndNovelIdAndTag(Long userId, Long novelId, String tag);

    /**
     * 查询用户的标签审核记录（分页）
     */
    Page<TagAudit> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户和审核状态查询（分页）
     */
    Page<TagAudit> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Integer status, Pageable pageable);

    /**
     * 根据审核状态查询（分页）
     */
    Page<TagAudit> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 查询书籍的所有标签审核记录
     */
    List<TagAudit> findByNovelIdAndStatus(Long novelId, Integer status);
}
