package com.plantapp;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageAnnotate {

    /**
     * Analyzes an image from a byte array and returns a list of labels.
     *
     * @param imageBytes The raw byte data of the image.
     * @return A List of strings, where each string is a detected label.
     * @throws IOException If there's an API communication error.
     */
    public List<String> analyzeImage(byte[] imageBytes) throws IOException {
        // Initialize a client in a try-with-resources block to ensure it's closed automatically.
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            ByteString imgBytes = ByteString.copyFrom(imageBytes);

            // Builds the image annotation request
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            // Performs label detection on the image file
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            // Processes the response and returns raw data
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    // In a web app, it's better to throw an exception that the controller can handle
                    throw new IOException("Error from Vision API: " + res.getError().getMessage());
                }

                // Return a list of the label descriptions. The frontend will format them.
                return res.getLabelAnnotationsList().stream()
                        .map(annotation -> String.format(
                                "%s (%.0f%%)", // Simplified format for the web
                                annotation.getDescription(),
                                annotation.getScore() * 100
                        ))
                        .collect(Collectors.toList());
            }

            // Return an empty list if no responses are found (should be rare)
            return new ArrayList<>();
        }

    }
    // This is to support the desktop application
    public String detectLabels(String filePath) throws IOException {
        // Read the file from the given path into a byte array
        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);

        // Call the new, core method to get the raw labels
        List<String> labels = analyzeImage(data);

        // Format the raw list into the nice string that the JTextArea expects
        if (labels.isEmpty()) {
            return "No labels detected.";
        }

        StringBuilder resultBuilder = new StringBuilder("Detected Labels:\n\n");
        for (String label : labels) {
            resultBuilder.append("  • ").append(label).append("\n");
        }
        return resultBuilder.toString();
    }
}
