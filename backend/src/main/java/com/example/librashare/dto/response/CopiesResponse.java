package com.example.librashare.dto.response;

import com.example.librashare.domain.CopyStatus;

/**
 * CopiesのResponseクラス
 * @author ichikura
 * @since 2026-07-15
 */
public class CopiesResponse {

    private Long id;
    private CopyStatus status;

    public CopiesResponse() {
    }

    public CopiesResponse(Long id, CopyStatus status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public CopyStatus getStatus() {
        return status;
    }

    
    

}
