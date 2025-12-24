package com.myapp.project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.database.Cursor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.project.ai_assistant.TextRecognitionHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// SỬA: Import nút tròn
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_FILE_PICKER = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;

    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Document> documentList;
    private EditText etSearch;
    private ImageButton btnAccount;
    private TextView tvWelcome;

    // SỬA: Khai báo là Nút Tròn (FloatingActionButton)
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabLibrary;

    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(getApplicationContext());
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        setupRecyclerView();
        loadDocuments();
        setupSearch();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String name = user.getEmail().split("@")[0];
            tvWelcome.setText("Xin chào, " + name);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.etSearch);

        // Ánh xạ (Sẽ không còn lỗi ClassCastException nữa)
        fabAdd = findViewById(R.id.fabAdd);
        fabLibrary = findViewById(R.id.fabLibrary);

        btnAccount = findViewById(R.id.btnAccount);
        tvWelcome = findViewById(R.id.tvWelcome);

        dbHelper = new DatabaseHelper(this);

        fabLibrary.setOnClickListener(v ->
                startActivity(new Intent(this, LibraryActivity.class)));

        fabAdd.setOnClickListener(v -> showAddDocumentDialog());

        btnAccount.setOnClickListener(v -> showAccountMenu());
    }

    // ... (CÁC HÀM BÊN DƯỚI GIỮ NGUYÊN KHÔNG CẦN COPY LẠI) ...
    // Bạn chỉ cần thay thế phần đầu file đến hết hàm initViews() là được.
    // Hoặc nếu muốn chắc ăn, hãy copy lại toàn bộ các hàm logic từ tin nhắn trước vào đây.

    // --- Code logic giữ nguyên để tránh dài dòng ---
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        documentList = dbHelper.getAllDocuments();
        adapter = new DocumentAdapter(this, documentList);
        adapter.setOnDocumentLongClickListener(this::showDocumentOptions);
        recyclerView.setAdapter(adapter);
    }
    // ...
    private void loadDocuments() {
        documentList = dbHelper.getAllDocuments();
        if (adapter != null) adapter.updateList(documentList);
    }
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) loadDocuments();
                else adapter.updateList(dbHelper.searchDocuments(s.toString()));
            }
        });
    }
    private void showAddDocumentDialog() {
        String[] options = {"Chọn file PDF", "Chọn file TXT", "Chọn ảnh", "Chụp ảnh tài liệu"};
        new AlertDialog.Builder(this).setTitle("Thêm tài liệu").setItems(options, (d, w) -> {
            if (w == 0) openFilePicker("application/pdf");
            else if (w == 1) openFilePicker("text/plain");
            else if (w == 2) openFilePicker("image/*");
            else dispatchTakePictureIntent();
        }).show();
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
        if (req == REQUEST_FILE_PICKER && res == RESULT_OK && data != null) showDocumentInfoDialog(data.getData());
        if (req == REQUEST_IMAGE_CAPTURE && res == RESULT_OK) showDocumentInfoDialog(photoUri);
    }
    private void showDocumentInfoDialog(Uri uri) {
        LayoutInflater inflater = LayoutInflater.from(this);
        android.view.View view = inflater.inflate(R.layout.dialog_add_document, null);
        EditText etName = view.findViewById(R.id.etDocName);
        EditText etDesc = view.findViewById(R.id.etDocDescription);
        EditText etTags = view.findViewById(R.id.etDocTags);
        etName.setText(getFileName(uri));
        new AlertDialog.Builder(this).setTitle("Thông tin tài liệu").setView(view).setPositiveButton("Lưu", (d, w) ->
                saveDocument(uri, etName.getText().toString(), etDesc.getText().toString(), etTags.getText().toString())).setNegativeButton("Hủy", null).show();
    }
    private void saveDocument(Uri uri, String name, String desc, String tags) {
        try {
            File dest = new File(getFilesDir(), System.currentTimeMillis() + "_" + getFileName(uri));
            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
            in.close();
            out.close();
            String fileName = dest.getName().toLowerCase();
            String fileType = "OTHER";
            if (fileName.endsWith(".pdf")) fileType = "PDF";
            else if (fileName.endsWith(".txt")) fileType = "TXT";
            else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) fileType = "IMAGE";
            Document doc = new Document();
            doc.setName(name);
            doc.setDescription(desc);
            doc.setTags(tags);
            doc.setFilePath(dest.getAbsolutePath());
            doc.setFileType(fileType);
            if (fileType.equals("IMAGE")) {
                Toast.makeText(this, "Đang quét chữ...", Toast.LENGTH_SHORT).show();
                TextRecognitionHelper.recognizeTextFromImage(this, uri, new TextRecognitionHelper.OnOCRListener() {
                    @Override public void onSuccess(String text) { doc.setExtractedText(text); dbHelper.insertDocument(doc); loadDocuments(); }
                    @Override public void onFailure(Exception e) { dbHelper.insertDocument(doc); loadDocuments(); }
                });
            } else if (fileType.equals("PDF")) {
                Toast.makeText(this, "Đang đọc PDF...", Toast.LENGTH_SHORT).show();
                PdfTextHelper.extractTextFromPdf(this, dest, new PdfTextHelper.OnPdfExtractListener() {
                    @Override public void onSuccess(String text) { if (text.length() > 30000) text = text.substring(0, 30000) + "..."; doc.setExtractedText(text); dbHelper.insertDocument(doc); loadDocuments(); Toast.makeText(MainActivity.this, "Xong!", Toast.LENGTH_SHORT).show(); }
                    @Override public void onFailure(Exception e) { doc.setExtractedText("Lỗi đọc PDF"); dbHelper.insertDocument(doc); loadDocuments(); }
                });
            } else { dbHelper.insertDocument(doc); loadDocuments(); }
        } catch (Exception e) { Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    }
    private String getFileName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int index = c.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME);
                return c.getString(index);
            }
        } catch (Exception ignored) {}
        return "document";
    }
    private void showDocumentOptions(Document doc, int pos) {
        String[] ops = {"Chỉnh sửa", "Xóa", "Thêm vào thư viện"};
        new AlertDialog.Builder(this).setTitle(doc.getName()).setItems(ops, (d, w) -> {
            if (w == 0) showEditDialog(doc);
            else if (w == 1) deleteDocument(doc, pos);
            else showLibraryChooser(doc);
        }).show();
    }
    private void showLibraryChooser(Document doc) {
        var libs = dbHelper.getAllLibraries();
        if (libs.isEmpty()) { Toast.makeText(this, "Chưa có thư viện!", Toast.LENGTH_SHORT).show(); return; }
        String[] names = new String[libs.size()];
        for (int i = 0; i < libs.size(); i++) names[i] = libs.get(i).getName();
        new AlertDialog.Builder(this).setTitle("Chọn thư viện").setItems(names, (d, w) -> dbHelper.addDocumentToLibrary(libs.get(w).getId(), doc.getId())).show();
    }
    private void showEditDialog(Document doc) {
        LayoutInflater inflater = LayoutInflater.from(this);
        android.view.View v = inflater.inflate(R.layout.dialog_add_document, null);
        ((EditText) v.findViewById(R.id.etDocName)).setText(doc.getName());
        ((EditText) v.findViewById(R.id.etDocDescription)).setText(doc.getDescription());
        ((EditText) v.findViewById(R.id.etDocTags)).setText(doc.getTags());
        new AlertDialog.Builder(this).setTitle("Chỉnh sửa").setView(v).setPositiveButton("Lưu", (d, w) -> {
            doc.setName(((EditText) v.findViewById(R.id.etDocName)).getText().toString());
            doc.setDescription(((EditText) v.findViewById(R.id.etDocDescription)).getText().toString());
            doc.setTags(((EditText) v.findViewById(R.id.etDocTags)).getText().toString());
            dbHelper.updateDocument(doc);
            loadDocuments();
        }).setNegativeButton("Hủy", null).show();
    }
    private void deleteDocument(Document doc, int pos) { dbHelper.deleteDocument(doc.getId()); adapter.removeItem(pos); Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show(); }
    private void showAccountMenu() {
        PopupMenu menu = new PopupMenu(this, btnAccount);
        menu.getMenuInflater().inflate(R.menu.menu_account, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_switch_account) switchAccount(); else logout(); return true;
        });
        menu.show();
    }
    private void switchAccount() { new AlertDialog.Builder(this).setTitle("Chuyển tài khoản").setMessage("Bạn có muốn chuyển tài khoản không?").setPositiveButton("Có", (d, w) -> logout()).setNegativeButton("Hủy", null).show(); }
    private void logout() { FirebaseAuth.getInstance().signOut(); Intent intent = new Intent(MainActivity.this, LoginActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(intent); finish(); }
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (Exception ex) { }
            if (photoFile != null) {
                photoUri = androidx.core.content.FileProvider.getUriForFile(this, "com.myapp.project.fileprovider", photoFile);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
}