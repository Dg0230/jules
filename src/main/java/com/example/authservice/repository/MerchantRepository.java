package com.example.authservice.repository;

import com.example.authservice.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByName(String name);
    boolean existsByName(String name);

    Page<Merchant> findByChannelId(Long channelId, Pageable pageable);
    List<Merchant> findByChannelId(Long channelId); // For non-paginated results if needed

    Page<Merchant> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
