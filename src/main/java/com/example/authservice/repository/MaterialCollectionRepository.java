package com.example.authservice.repository;

import com.example.authservice.entity.MaterialCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaterialCollectionRepository extends JpaRepository<MaterialCollection, Long> {
    Optional<MaterialCollection> findByName(String name);
    boolean existsByName(String name);

    Page<MaterialCollection> findByCreatorId(Long creatorId, Pageable pageable);
}
