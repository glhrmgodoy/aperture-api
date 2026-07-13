package com.godoy.aperture.repository;

import com.godoy.aperture.domain.entity.CustomList;
import com.godoy.aperture.domain.enums.ListVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomListRepository extends JpaRepository<CustomList, UUID> {

    Page<CustomList> findByUserId(UUID userId, Pageable pageable);

    Page<CustomList> findByVisibility(ListVisibility visibility, Pageable pageable);
}
