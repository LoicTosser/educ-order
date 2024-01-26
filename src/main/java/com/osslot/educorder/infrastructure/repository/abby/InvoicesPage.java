package com.osslot.educorder.infrastructure.repository.abby;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class InvoicesPage {
    private List<Invoice> docs;
    private Integer countWithoutFilters;
    private Integer totalDocs;
    private Integer limit;
    private Integer totalPages;
    private Boolean hasNextPage;
    private Boolean hasPrevPage;
    private Integer nextPage;
    private Integer page;
    private Integer prevPage;
}
