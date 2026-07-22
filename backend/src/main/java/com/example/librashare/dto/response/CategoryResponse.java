package com.example.librashare.dto.response;

/**
 * CategoryResponseのレスポンスクラス
 * @author furuyama
 * @since 2026-07-22
 * @see BookReponse
 */
public class CategoryResponse {
    private Long smallId;
    private String smallName;
    private Long mediumId;
    private String mediumName;
    private Long largeId;
    private String largeName;
    
    public CategoryResponse() {
    }

    public CategoryResponse(Long smallId, String smallName, Long mediumId, String mediumName, Long largeId,
            String largeName) {
        this.smallId = smallId;
        this.smallName = smallName;
        this.mediumId = mediumId;
        this.mediumName = mediumName;
        this.largeId = largeId;
        this.largeName = largeName;
    }

    public Long getSmallId() {
        return smallId;
    }

    public void setSmallId(Long smallId) {
        this.smallId = smallId;
    }

    public String getSmallName() {
        return smallName;
    }

    public void setSmallName(String smallName) {
        this.smallName = smallName;
    }

    public Long getMediumId() {
        return mediumId;
    }

    public void setMediumId(Long mediumId) {
        this.mediumId = mediumId;
    }

    public String getMediumName() {
        return mediumName;
    }

    public void setMediumName(String mediumName) {
        this.mediumName = mediumName;
    }

    public Long getLargeId() {
        return largeId;
    }

    public void setLargeId(Long largeId) {
        this.largeId = largeId;
    }

    public String getLargeName() {
        return largeName;
    }

    public void setLargeName(String largeName) {
        this.largeName = largeName;
    }

    
}
