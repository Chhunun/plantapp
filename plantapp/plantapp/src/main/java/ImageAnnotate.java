import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageAnnotate {

    /**
     * Detects labels in a local image file using the Google Cloud Vision API.
     *
     * @param filePath The path to the local file to analyze.
     * @return A formatted string of detected labels and their scores.
     * @throws IOException If the file cannot be read or there's an API error.
     */
    public String detectLabels(String filePath) throws IOException {
        // Initialize a client in a try-with-resources block to ensure it's closed automatically.
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            // Read the image file into a byte string
            Path path = Paths.get(filePath);
            byte[] data = Files.readAllBytes(path);
            ByteString imgBytes = ByteString.copyFrom(data);

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

            StringBuilder resultBuilder = new StringBuilder("Google Vision API Results:\n--------------------------\n");

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.err.printf("Error: %s%n", res.getError().getMessage());
                    return "Error analyzing image: " + res.getError().getMessage();
                }

                if (res.getLabelAnnotationsList().isEmpty()) {
                    resultBuilder.append("No labels detected for this image.");
                } else {
                    // Convert labels to a formatted string
                    String labels = res.getLabelAnnotationsList().stream()
                            .map(annotation -> String.format(
                                    "- %s (%.2f%% score)",
                                    annotation.getDescription(),
                                    annotation.getScore() * 100
                            ))
                            .collect(Collectors.joining("\n"));
                    resultBuilder.append(labels);
                }
            }
            return resultBuilder.toString();
        }
    }
}