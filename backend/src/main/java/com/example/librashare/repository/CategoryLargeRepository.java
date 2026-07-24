package com.example.librashare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librashare.domain.CategoryLarge;

public interface CategoryLargeRepository extends JpaRepository<CategoryLarge, Long> {
    List<CategoryLarge> findAllByOrderBySortOrderAscIdAsc();
}
