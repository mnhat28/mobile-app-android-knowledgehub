package com.myapp.project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.database.Cursor;
import com.google.firebase.auth.FirebaseAuth;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_FILE_PICKER = 101;

    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Document> documentList;
    private EditText etSearch;
    private FloatingActionButton fabAdd, fabLibrary;
    private ImageButton btnAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        setupRecyclerView();
        loadDocuments();
        setupSearch();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.etSearch);
        fabAdd = findViewById(R.id.fabAdd);
        fabLibrary = findViewById(R.id.fabLibrary);
        btnAccount = findViewById(R.id.btnAccount);

        dbHelper = new DatabaseHelper(this);

        fabLibrary.setOnClickListener(v ->
                startActivity(new Intent(this, LibraryActivity.class)));

        fabAdd.setOnClickListener(v -> showAddDocumentDialog());

        btnAccount.setOnClickListener(v -> showAccountMenu());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        documentList = dbHelper.getAllDocuments();
        adapter = new DocumentAdapter(this, documentList);
        adapter.setOnDocumentLongClickListener(this::showDocumentOptions);
        recyclerView.setAdapter(adapter);
    }

    private void loadDocuments() {
        documentList = dbHelper.getAllDocuments();
        if (adapter != null) adapter.updateList(documentList);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) loadDocuments();
                else adapter.updateList(dbHelper.searchDocuments(s.toString()));
            }
        });
    }

    private void showAddDocumentDialog() {
        String[] options = {"Chọn file PDF", "Chọn file TXT", "Chọn ảnh"};

        new AlertDialog.Builder(this)
                .setTitle("Thêm tài liệu")
                .setItems(options, (d, w) -> {
                    if (w == 0) openFilePicker("application/pdf");
                    else if (w == 1) openFilePicker("text/plain");
                    else openFilePicker("image/*");
                })
                .show();
    }

    private void openFilePicker(String mime) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType(mime);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(i, "Chọn file"), REQUEST_FILE_PICKER);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQUEST_FILE_PICKER && res == RESULT_OK && data != null)
            showDocumentInfoDialog(data.getData());
    }

    private void showDocumentInfoDialog(Uri uri) {
        var view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_document, null);

        EditText etName = view.findViewById(R.id.etDocName);
        EditText etDesc = view.findViewById(R.id.etDocDescription);
        EditText etTags = view.findViewById(R.id.etDocTags);

        etName.setText(getFileName(uri));

        new AlertDialog.Builder(this)
                .setTitle("Thông tin tài liệu")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) ->
                        saveDocument(uri,
                                etName.getText().toString(),
                                etDesc.getText().toString(),
                                etTags.getText().toString()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveDocument(Uri uri, String name, String desc, String tags) {
        try {
            File dest = new File(getFilesDir(),
                    System.currentTimeMillis() + "_" + getFileName(uri));

            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(dest);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);

            in.close();
            out.close();

            String type = dest.getName().toLowerCase().endsWith(".pdf") ? "PDF" :
                    dest.getName().endsWith(".txt") ? "TXT" : "IMAGE";

            Document doc = new Document();
            doc.setName(name);
            doc.setDescription(desc);
            doc.setTags(tags);
            doc.setFilePath(dest.getAbsolutePath());
            doc.setFileType(type);

            dbHelper.insertDocument(doc);
            loadDocuments();

            Toast.makeText(this, "Đã lưu!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int index = c.getColumnIndexOrThrow(
                        android.provider.OpenableColumns.DISPLAY_NAME);
                return c.getString(index);
            }
        } catch (Exception ignored) {}
        return "document";
    }


    private void showDocumentOptions(Document doc, int pos) {
        String[] ops = {"Chỉnh sửa", "Xóa", "Thêm vào thư viện"};

        new AlertDialog.Builder(this)
                .setTitle(doc.getName())
                .setItems(ops, (d, w) -> {
                    if (w == 0) showEditDialog(doc);
                    else if (w == 1) deleteDocument(doc, pos);
                    else showLibraryChooser(doc);
                })
                .show();
    }

    private void showLibraryChooser(Document doc) {
        var libs = dbHelper.getAllLibraries();
        if (libs.isEmpty()) {
            Toast.makeText(this, "Chưa có thư viện!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[libs.size()];
        for (int i = 0; i < libs.size(); i++) names[i] = libs.get(i).getName();

        new AlertDialog.Builder(this)
                .setTitle("Chọn thư viện")
                .setItems(names, (d, w) ->
                        dbHelper.addDocumentToLibrary(libs.get(w).getId(), doc.getId()))
                .show();
    }

    private void showEditDialog(Document doc) {
        var v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_document, null);

        ((EditText) v.findViewById(R.id.etDocName)).setText(doc.getName());
        ((EditText) v.findViewById(R.id.etDocDescription)).setText(doc.getDescription());
        ((EditText) v.findViewById(R.id.etDocTags)).setText(doc.getTags());

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa")
                .setView(v)
                .setPositiveButton("Lưu", (d, w) -> {
                    doc.setName(((EditText) v.findViewById(R.id.etDocName)).getText().toString());
                    doc.setDescription(((EditText) v.findViewById(R.id.etDocDescription)).getText().toString());
                    doc.setTags(((EditText) v.findViewById(R.id.etDocTags)).getText().toString());
                    dbHelper.updateDocument(doc);
                    loadDocuments();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteDocument(Document doc, int pos) {
        dbHelper.deleteDocument(doc.getId());
        adapter.removeItem(pos);
        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
    }

    private void showAccountMenu() {
        PopupMenu menu = new PopupMenu(this, btnAccount);
        menu.getMenuInflater().inflate(R.menu.menu_account, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_switch_account) switchAccount();
            else logout();
            return true;
        });
        menu.show();
    }

    private void switchAccount() {
        Intent intent = new Intent(MainActivity.this, AccountChooserActivity.class);
        startActivity(intent);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut(); // 🔥 DÒNG QUAN TRỌNG NHẤT

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_PERMISSION);
        }
    }
}
