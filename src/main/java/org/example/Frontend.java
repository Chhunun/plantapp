package org.example;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class Frontend {

    private final JFrame frame;
    private final JLabel selectedFilePathLabel;
    private final JTextArea resultsArea;
    private final JButton annotateButton;

    public Frontend() {
        // --- Frame Setup ---
        frame = new JFrame("Image Annotator");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // --- Top Panel for File Selection ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("1. Select an Image File"));

        selectedFilePathLabel = new JLabel("No image selected...", SwingConstants.CENTER);
        selectedFilePathLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        topPanel.add(selectedFilePathLabel, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> openFileChooser());
        topPanel.add(browseButton, BorderLayout.WEST);

        // --- Bottom Panel for Results and Action ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("2. Annotate and View Results"));

        resultsArea = new JTextArea("Annotation results will appear here.");
        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        annotateButton = new JButton("Annotate Image");
        annotateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        annotateButton.setEnabled(false); // Disabled until a file is chosen
        bottomPanel.add(annotateButton, BorderLayout.SOUTH);

        // --- Assembling the Frame ---
        ((JPanel) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null); // Center the frame on the screen
    }

    public void show() {
        frame.setVisible(true);
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose an Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files (jpg, png, gif)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            prepareForAnnotation(selectedFile);
        }
    }

    private void prepareForAnnotation(File imageFile) {
        String path = imageFile.getAbsolutePath();
        selectedFilePathLabel.setText(path);
        resultsArea.setText("Ready to annotate '" + imageFile.getName() + "'.");
        annotateButton.setEnabled(true);

        // Remove previous listeners to avoid multiple triggers
        for (var listener : annotateButton.getActionListeners()) {
            annotateButton.removeActionListener(listener);
        }

        // Add a new listener for the currently selected file
        annotateButton.addActionListener(e -> runAnnotation(path, imageFile.getName()));
    }

    private void runAnnotation(String imagePath, String imageName) {
        annotateButton.setEnabled(false);
        annotateButton.setText("Annotating...");
        resultsArea.setText("Contacting Google Vision API for '" + imageName + "'...");

        // Use SwingWorker to run the network call on a background thread.
        // This prevents the UI from freezing while waiting for the API.
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // This happens on a background thread
                ImageAnnotate annotator = new ImageAnnotate();
                return annotator.detectLabels(imagePath);
            }

            @Override
            protected void done() {
                // This happens back on the UI thread after doInBackground() is finished
                try {
                    String resultText = get(); // Get the result from the background task.
                    resultsArea.setText(resultText);
                } catch (Exception ex) {
                    ex.printStackTrace(); // Log the full stack trace for debugging.
                    String errorMessage = "An error occurred with the Google Vision API: "
                            + ex.getMessage()
                            + "\n\nThis can happen if authentication is not set up correctly.";
                    resultsArea.setText(errorMessage);
                    JOptionPane.showMessageDialog(frame, errorMessage, "API Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    annotateButton.setText("Annotate Image");
                    annotateButton.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        // It's best practice to create and show Swing GUIs on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            Frontend frontend = new Frontend();
            frontend.show();
        });
    }
}