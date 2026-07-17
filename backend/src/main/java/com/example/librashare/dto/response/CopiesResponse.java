package com.example.librashare.dto.response;

import com.example.librashare.domain.CopyStatus;

public class CopiesResponse {

    private Long id;
    private CopyStatus copystatus;

    public CopiesResponse() {
    }

    public CopiesResponse(Long id, CopyStatus copystatus) {
        this.id = id;
        this.copystatus = copystatus;
    }

    public Long getId() {
        return id;
    }

    public CopyStatus getCopystatus() {
        return copystatus;
    }

    
    

}
