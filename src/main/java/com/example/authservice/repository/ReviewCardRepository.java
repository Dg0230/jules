package com.example.authservice.repository;

import com.example.authservice.entity.ReviewCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCardRepository extends JpaRepository<ReviewCard, Long> {
    Optional<ReviewCard> findByName(String name);
    Optional<ReviewCard> findByCardIdentifier(String cardIdentifier); // Based on schema
    boolean existsByName(String name);
    boolean existsByCardIdentifier(String cardIdentifier);

    Page<ReviewCard> findByMerchantId(Long merchantId, Pageable pageable);
}
