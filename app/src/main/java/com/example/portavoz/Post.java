package com.example.portavoz;

import java.util.List;

public class Post {
    public String id_, username, userId, userImage, created, title, desc, status, address, updated;
    public boolean isUpvoted = false, hasmore;
    public int likes, comments, v__;
    public double lon, lat;
    public List<String> images;
    public List<String> hashtags, hashtagsIds;

    public Post(String id_, String title, List<String> images){
        this.id_ = id_;
        this.title = title;
        this.images = images;
    }

    public Post(String userId, String username, String userImage, // user credentials
                String id_, String title, String desc, List<String> images, String status, // post data
                String created, String updated, // post date
                double lon, double lat, String address, // post address
                int v__, List<String> hashtags, List<String> hashtagsIds, boolean hasmore, boolean isUpvoted, int likes, int comments){ // post others

        /// user credentials
        this.username = username;
        this.userId = userId;
        this.userImage = userImage;

        /// post credentials
        // data
        this.id_ = id_;
        this.title = title;
        this.desc = desc;
        this.images = images;
        this.status = status;

        // date
        this.created = created;
        this.updated = updated;

        // address
        this.lon = lon;
        this.lat = lat;
        this.address = address;

        // other
        this.v__ = v__;
        this.hashtags = hashtags;
        this.hashtagsIds = hashtagsIds;
        this.hasmore = hasmore;
        this.isUpvoted = isUpvoted;
        this.likes = likes;
        this.comments = comments;
    }
}
