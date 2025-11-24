package com.example.portavoz.createPost;

import java.util.List;

public class ValidationResponse {
    public boolean valid;
    public List<ValidationError> errors;
    public List<String> suggestion;

    public class ValidationError {
        public String field;
        public String message;
    }
}
