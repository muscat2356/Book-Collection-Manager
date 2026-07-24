package com.example.librashare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.librashare.dto.response.CategoryNodeResponse;
import com.example.librashare.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 大中小カテゴリツリー取得
     * @return 200 ネスト配列
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('general_employee','admin_employee')")
    public ResponseEntity<List<CategoryNodeResponse>> getTree() {
        return ResponseEntity.ok(categoryService.getTree());
    }

}
