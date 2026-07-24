package com.odschool.dtos;

import com.odschool.enums.SearchStatus;
import lombok.Data;

@Data
public class SearchRequest {
    private int standardId;
    private int divisionId;

    private SearchStatus searchStatus;

    public SearchRequest(int standardId, int divisionId) {
        this.standardId = standardId;
        this.divisionId = divisionId;
    }
}
