package com.example.librashare.dto.response;

import com.example.librashare.domain.CopyStatus;

public class HoldingResponse {
    private Long id;
    private CopyStatus status;

    public HoldingResponse() {
    }

    public HoldingResponse(Long id, CopyStatus status) {
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
