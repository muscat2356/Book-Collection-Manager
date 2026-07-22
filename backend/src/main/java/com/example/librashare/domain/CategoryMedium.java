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
 * CategoryMediumテーブルのdomainクラス
 * @author furuyama
 * @since 2026-07-22
 * @see CategorySmall
 * @see CategoryLarge
 */
@Entity
@Table(name = "category_medium")
public class CategoryMedium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "large_id", nullable = false)
    private CategoryLarge categoryLarge;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public CategoryMedium() {
    }

    public CategoryMedium(Long id, CategoryLarge categoryLarge, String name, Integer sortOrder) {
        this.id = id;
        this.categoryLarge = categoryLarge;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoryLarge getCategoryLarge() {
        return categoryLarge;
    }

    public void setCategoryLarge(CategoryLarge categoryLarge) {
        this.categoryLarge = categoryLarge;
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

}
