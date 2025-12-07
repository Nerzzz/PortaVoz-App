package com.example.portavoz.post.comments;

import com.google.gson.annotations.SerializedName;

public class Comment {

    public String id;
    public String parentId;

    public String content;

    public ParentType parentType;

    @SerializedName("user")
    public UserData user;

    public int repliesCount;
    public boolean isUpvoted;

    public String createdAt;

    public static class UserData {
        public String id;
        public String username;
        public String image;
    }

    public enum ParentType {
        POST("Post"),
        COMMENT("Comment");

        private final String value;

        ParentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ParentType fromString(String text) {
            for (ParentType t : ParentType.values()) {
                if (t.value.equalsIgnoreCase(text)) {
                    return t;
                }
            }
            return null;
        }
    }
}
