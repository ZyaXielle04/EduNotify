package com.zyacodes.edunotifyproj.Models;

import java.util.Map;

public class Post {
    private String postId; // Firebase key
    private String content;
    private String postedBy;
    private long timestamp;
    private Map<String, Boolean> likedBy; // UID -> true
    private Map<String, Comment> comments; // commentId -> Comment

    // Default constructor for Firebase
    public Post() {}

    public Post(String postId, String content, String postedBy, long timestamp) {
        this.postId = postId;
        this.content = content;
        this.postedBy = postedBy;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Boolean> getLikedBy() { return likedBy; }
    public void setLikedBy(Map<String, Boolean> likedBy) { this.likedBy = likedBy; }

    public Map<String, Comment> getComments() { return comments; }
    public void setComments(Map<String, Comment> comments) { this.comments = comments; }

    // Helper method: check if current user liked this post
    public boolean isLikedByUser(String uid) {
        return likedBy != null && likedBy.containsKey(uid);
    }

    // Inner class for Comment
    public static class Comment {
        private String content;
        private String postedBy;
        private long timestamp;

        public Comment() {}

        public Comment(String content, String postedBy, long timestamp) {
            this.content = content;
            this.postedBy = postedBy;
            this.timestamp = timestamp;
        }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getPostedBy() { return postedBy; }
        public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
