package com.example.portavoz.post.comments;

public class Comment {

    public String id;
    public String parentId;

    public String content;

    public ParentType parentType;

    public String userId;
    public String userName;
    public String userImage;

    public int repliesCount;
    public boolean isUpvoted;

    public String createdAt;

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


    public Comment(String id, String parentId, ParentType parentType,
                   String content, String userId, String userName, String userImage,
                   int repliesCount, boolean isUpvoted, String createdAt) {

        //data
        this.id = id;
        this.parentId = parentId;
        this.parentType = parentType;
        this.content = content;

        //credenciais
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;

        //outros
        this.repliesCount = repliesCount;
        this.isUpvoted = isUpvoted;
        this.createdAt = createdAt;
    }
}
