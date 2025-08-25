package com.gooners.watguessr.dto;

import java.util.List;

public class QueryResults<T> {
    private List<T> results;
    private long totalCount;

    public QueryResults() {}

    public QueryResults(List<T> results) {
        this.results = results;
    }

    public QueryResults(List<T> results, long totalCount) {
        this.results = results;
        this.totalCount = totalCount;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
