package com.example.librashare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * CategorySmallテーブルのdomainクラス
 * CategoryMediumと多対一の関係
 * @author furuyama
 * @since 2026-07-22
 * @see CategoryMedium.java
 * @see Book.java
 * @see BooksController
 */

@Entity
@Table(name = "category_small")
public class CategorySmall {

    //主キー宣言
    //主キーの値を自動生成 -> DBの自動採番をもとに設定
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medium_id", nullable= false)
    private CategoryMedium categoryMedium;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public CategorySmall() {
    }

    public CategorySmall(Long id, CategoryMedium categoryMedium, String name, Integer sortOrder) {
        this.id = id;
        this.categoryMedium = categoryMedium;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public CategoryMedium getCategoryMedium() {
        return categoryMedium;
    }

    public void setCategoryMedium(CategoryMedium categoryMedium) {
        this.categoryMedium = categoryMedium;
    }
    

}
