package com.example.librashare.exception.exception;

import java.util.List;

public class CopyNotAvailableException extends RuntimeException {

    private final List<Long> failedBookCopyIds;

    public CopyNotAvailableException (List<Long> failedBookCopyIds){
        super("貸出できない所蔵が含まれています");
        this.failedBookCopyIds = failedBookCopyIds;
    }


    public String getError() {
        return "COPY_NOT_AVAILABLE";
    }


    public List<Long> getFailedBookCopyIds() {
        return failedBookCopyIds;
    }

    
}
