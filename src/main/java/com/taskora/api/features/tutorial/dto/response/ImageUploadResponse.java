package com.taskora.api.features.tutorial.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageUploadResponse {

    private String imageUrl;

    public ImageUploadResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
