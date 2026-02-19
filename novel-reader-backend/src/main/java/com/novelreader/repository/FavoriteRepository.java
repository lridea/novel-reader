package com.novelreader.repository;

import com.novelreader.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndNovelId(Long userId, Long novelId);

    Optional<Favorite> findByUserIdAndNovelIdAndCategoryId(Long userId, Long novelId, Long categoryId);

    List<Favorite> findByUserIdAndNovelIdIn(Long userId, List<Long> novelIds);

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    Page<Favorite> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    boolean existsByUserIdAndNovelIdAndCategoryId(Long userId, Long novelId, Long categoryId);

    void deleteByUserIdAndNovelId(Long userId, Long novelId);

    void deleteByUserIdAndNovelIdAndCategoryId(Long userId, Long novelId, Long categoryId);

    long countByUserId(Long userId);

    long countByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Favorite> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT f FROM Favorite f JOIN Novel n ON f.novelId = n.id WHERE f.userId = :userId AND n.title LIKE %:keyword%")
    Page<Favorite> findByUserIdAndNovelTitleContaining(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT f FROM Favorite f JOIN Novel n ON f.novelId = n.id WHERE f.userId = :userId AND f.categoryId = :categoryId AND n.title LIKE %:keyword%")
    Page<Favorite> findByUserIdAndCategoryIdAndNovelTitleContaining(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
}
