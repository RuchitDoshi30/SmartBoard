package com.smartboard.entity;

import javax.persistence.*;
import java.util.Date;
import javax.persistence.Column;

@Entity
@Table(name = "notices")   // ✅ matches your DB table name
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String description;
    private String priority;
    private String status;
    @Column(name = "attachment_path") 
private String attachmentPath;
    @Temporal(TemporalType.DATE)
    private Date date;
    
public String getDescription() {
    return this.description;
}

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    
    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}
