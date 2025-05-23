package com.example.authservice.repository;

import com.example.authservice.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByName(String name);
    boolean existsByName(String name);

    Page<Material> findByUploaderId(Long uploaderId, Pageable pageable);
    List<Material> findByIdIn(List<Long> ids); // For fetching materials by a list of IDs
}
