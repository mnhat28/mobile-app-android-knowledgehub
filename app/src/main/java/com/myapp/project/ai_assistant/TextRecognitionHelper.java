package com.myapp.project.ai_assistant;

import android.content.Context;
import android.net.Uri;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class TextRecognitionHelper {

    public interface OnOCRListener {
        void onSuccess(String text);

        void onFailure(Exception e);
    }

    public static void recognizeTextFromImage(Context context, Uri imageUri, OnOCRListener listener) {
        try {
            // Chuẩn bị hình ảnh từ Uri
            InputImage image = InputImage.fromFilePath(context, imageUri);

            // Khởi tạo bộ nhận dạng (Dành cho tiếng Latinh/Tiếng Việt không dấu)
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        // Trích xuất toàn bộ văn bản từ các khối (blocks)
                        StringBuilder resultText = new StringBuilder();
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            resultText.append(block.getText()).append("\n");
                        }
                        listener.onSuccess(resultText.toString());
                    })
                    .addOnFailureListener(listener::onFailure);

        } catch (Exception e) {
            listener.onFailure(e);
        }
    }
}
