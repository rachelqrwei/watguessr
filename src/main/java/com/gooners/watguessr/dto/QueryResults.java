package com.gooners.watguessr.dto;

import java.util.List;

public class QueryResults<T> {
    private List<T> results;

    public QueryResults() {}

    public QueryResults(List<T> results) {
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
