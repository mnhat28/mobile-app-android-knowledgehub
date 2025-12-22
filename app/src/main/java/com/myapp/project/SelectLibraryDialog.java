package com.myapp.project;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import java.util.List;

public class SelectLibraryDialog {

    public static void show(Context context, long documentId) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Library> libs = db.getAllLibraries();

        if (libs.isEmpty()) {
            Toast.makeText(context, "Chưa có thư viện nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] libNames = new String[libs.size()];
        for (int i = 0; i < libs.size(); i++) libNames[i] = libs.get(i).getName();

        new AlertDialog.Builder(context)
                .setTitle("Thêm vào thư viện")
                .setItems(libNames, (dialog, which) -> {
                    long libId = libs.get(which).getId();
                    long result = db.addDocumentToLibrary(libId, documentId);

                    if (result == -1)
                        Toast.makeText(context, "Tài liệu đã có trong thư viện này!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, "Đã thêm vào " + libs.get(which).getName(), Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
