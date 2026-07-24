package com.example.librashare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.librashare.domain.CategorySmall;

public interface CategorySmallRepository extends JpaRepository<CategorySmall, Long> {
    List<CategorySmall> findAllByOrderBySortOrderAscIdAsc();
    
} 
