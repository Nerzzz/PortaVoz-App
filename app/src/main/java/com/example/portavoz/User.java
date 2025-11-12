package com.example.portavoz;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User {
    public String id_, username, email, fName, lName, image, about, banner, reportsResetAt, createdA, upDatedAt;
    public int followers, following, unreadNofications, remaningReports, totalReports, __v;

    public User(String username, String fName, String lName, String about, String image, String banner){
        this.username = username;
        this.fName = fName;
        this.lName = lName;
        this.about = about;
        this.image = image;
        this.banner = banner;
    }
}
