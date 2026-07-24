package com.example.librashare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librashare.domain.CategoryMedium;

public interface CategoryMediumRepository extends JpaRepository<CategoryMedium, Long>{
    List<CategoryMedium> findAllByOrderBySortOrderAscIdAsc();
}
