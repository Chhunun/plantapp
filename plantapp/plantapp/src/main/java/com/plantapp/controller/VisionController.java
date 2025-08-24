package com.plantapp.controller;

import com.plantapp.ImageAnnotate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@RestController // Tells Spring this class handles web requests
public class VisionController {

    // This method will be available at the URL: http://localhost:8080/analyze
    @PostMapping("/analyze")
    public List<String> handleImageUpload(@RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        // @RequestParam("imageFile") automatically gets the uploaded file from the request.
        // Spring Boot handles the temporary storage and gives you an easy-to-use object.

        // Get the bytes from the uploaded file
        byte[] imageBytes = imageFile.getBytes();

        // Call your existing Vision API logic!
        // You would inject or create an instance of your VisionApi class here.
        ImageAnnotate visionApi = new ImageAnnotate();
        List<String> labels = visionApi.analyzeImage(imageBytes);

        // The List of strings will be automatically converted to JSON and sent
        // back to the browser as the response.
        return labels;
    }
}