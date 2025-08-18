package com.gooners.watguessr.dto;

public class BugReportRequest {
    private String subject;
    private String content;
    private String category;

    public BugReportRequest(String subject, String content, String category) {
        this.subject = subject;
        this.content = content;
        this.category = category;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
