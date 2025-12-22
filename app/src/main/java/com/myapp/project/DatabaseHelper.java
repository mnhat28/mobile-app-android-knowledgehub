package com.myapp.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "KnowledgeHub.db";

    // 🚨 QUAN TRỌNG: TĂNG VERSION ĐỂ TẠO BẢNG MỚI
    private static final int DATABASE_VERSION = 2;

    // Table Documents
    private static final String TABLE_DOCUMENTS = "documents";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_FILE_PATH = "file_path";
    private static final String COL_FILE_TYPE = "file_type";
    private static final String COL_TAGS = "tags";
    private static final String COL_CREATED_DATE = "created_date";
    private static final String COL_LAST_MODIFIED = "last_modified";

    // Table Libraries
    private static final String TABLE_LIBRARIES = "libraries";
    private static final String LIB_ID = "lib_id";
    private static final String LIB_NAME = "lib_name";
    private static final String LIB_TAGS = "lib_tags";
    private static final String LIB_DESCRIPTION = "lib_description";
    private static final String LIB_CREATED_DATE = "lib_created_date";

    // Join table library_documents
    private static final String TABLE_LIBRARY_DOCS = "library_documents";
    private static final String LD_ID = "id";
    private static final String LD_LIB_ID = "lib_id";
    private static final String LD_DOC_ID = "doc_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Documents table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_DOCUMENTS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_DESCRIPTION + " TEXT, " +
                        COL_FILE_PATH + " TEXT NOT NULL, " +
                        COL_FILE_TYPE + " TEXT NOT NULL, " +
                        COL_TAGS + " TEXT, " +
                        COL_CREATED_DATE + " INTEGER, " +
                        COL_LAST_MODIFIED + " INTEGER)"
        );

        // Libraries table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_LIBRARIES + " (" +
                        LIB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        LIB_NAME + " TEXT NOT NULL, " +
                        LIB_TAGS + " TEXT, " +
                        LIB_DESCRIPTION + " TEXT, " +
                        LIB_CREATED_DATE + " INTEGER)"
        );

        // Mapping table (library_documents)
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_LIBRARY_DOCS + " (" +
                        LD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        LD_LIB_ID + " INTEGER NOT NULL, " +
                        LD_DOC_ID + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + LD_LIB_ID + ") REFERENCES " + TABLE_LIBRARIES + "(" + LIB_ID + "), " +
                        "FOREIGN KEY(" + LD_DOC_ID + ") REFERENCES " + TABLE_DOCUMENTS + "(" + COL_ID + "))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Chỉ xóa khi nâng version → tránh crash app
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIBRARY_DOCS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIBRARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);

        onCreate(db);
    }

    // -------------------------------------
    // DOCUMENT METHODS
    // -------------------------------------

    public long insertDocument(Document document) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, document.getName());
        values.put(COL_DESCRIPTION, document.getDescription());
        values.put(COL_FILE_PATH, document.getFilePath());
        values.put(COL_FILE_TYPE, document.getFileType());
        values.put(COL_TAGS, document.getTags());
        values.put(COL_CREATED_DATE, document.getCreatedDate());
        values.put(COL_LAST_MODIFIED, document.getLastModified());

        long id = db.insert(TABLE_DOCUMENTS, null, values);
        db.close();
        return id;
    }

    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DOCUMENTS, null, null, null,
                null, null, COL_LAST_MODIFIED + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TAGS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_DATE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_LAST_MODIFIED))
                );
                documents.add(doc);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return documents;
    }

    public List<Document> searchDocuments(String query) {
        List<Document> documents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COL_NAME + " LIKE ? OR " + COL_DESCRIPTION + " LIKE ? OR " + COL_TAGS + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_DOCUMENTS, null, selection, selectionArgs,
                null, null, COL_LAST_MODIFIED + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TAGS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_DATE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_LAST_MODIFIED))
                );
                documents.add(doc);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return documents;
    }

    public int updateDocument(Document document) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, document.getName());
        values.put(COL_DESCRIPTION, document.getDescription());
        values.put(COL_TAGS, document.getTags());
        values.put(COL_LAST_MODIFIED, System.currentTimeMillis());

        int rows = db.update(TABLE_DOCUMENTS, values,
                COL_ID + " = ?", new String[]{String.valueOf(document.getId())});

        db.close();
        return rows;
    }

    public void deleteDocument(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_LIBRARY_DOCS, LD_DOC_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_DOCUMENTS, COL_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    // -------------------------------------
    // LIBRARY METHODS
    // -------------------------------------

    public long insertLibrary(Library library) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LIB_NAME, library.getName());
        values.put(LIB_TAGS, library.getTags());
        values.put(LIB_DESCRIPTION, library.getDescription());
        values.put(LIB_CREATED_DATE, library.getCreatedDate());
        long id = db.insert(TABLE_LIBRARIES, null, values);
        db.close();
        return id;
    }

    public List<Library> getAllLibraries() {
        List<Library> libs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LIBRARIES, null, null, null, null, null, LIB_CREATED_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Library lib = new Library(
                        cursor.getLong(cursor.getColumnIndexOrThrow(LIB_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LIB_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LIB_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LIB_TAGS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(LIB_CREATED_DATE))
                );
                libs.add(lib);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return libs;
    }

    public int updateLibrary(Library library) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LIB_NAME, library.getName());
        values.put(LIB_DESCRIPTION, library.getDescription());
        values.put(LIB_TAGS, library.getTags());
        int rows = db.update(TABLE_LIBRARIES, values,
                LIB_ID + " = ?", new String[]{String.valueOf(library.getId())});
        db.close();
        return rows;
    }

    public void deleteLibrary(long libId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LIBRARY_DOCS, LD_LIB_ID + " = ?", new String[]{String.valueOf(libId)});
        db.delete(TABLE_LIBRARIES, LIB_ID + " = ?", new String[]{String.valueOf(libId)});
        db.close();
    }

    public long addDocumentToLibrary(long libId, long docId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(
                TABLE_LIBRARY_DOCS,
                null,
                LD_LIB_ID + " = ? AND " + LD_DOC_ID + " = ?",
                new String[]{String.valueOf(libId), String.valueOf(docId)},
                null, null, null
        );

        boolean exists = c != null && c.getCount() > 0;
        if (c != null) c.close();
        db.close();

        if (exists) return -1;

        SQLiteDatabase wdb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LD_LIB_ID, libId);
        values.put(LD_DOC_ID, docId);
        long id = wdb.insert(TABLE_LIBRARY_DOCS, null, values);
        wdb.close();
        return id;
    }

    public void removeDocumentFromLibrary(long libId, long docId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LIBRARY_DOCS,
                LD_LIB_ID + " = ? AND " + LD_DOC_ID + " = ?",
                new String[]{String.valueOf(libId), String.valueOf(docId)});
        db.close();
    }

    public List<Document> getDocumentsInLibrary(long libId) {
        List<Document> docs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT d.* FROM " + TABLE_DOCUMENTS + " d " +
                        "INNER JOIN " + TABLE_LIBRARY_DOCS + " ld " +
                        "ON d." + COL_ID + " = ld." + LD_DOC_ID +
                        " WHERE ld." + LD_LIB_ID + " = ? " +
                        "ORDER BY d." + COL_LAST_MODIFIED + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(libId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TAGS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_DATE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_LAST_MODIFIED))
                );
                docs.add(doc);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return docs;
    }

    public List<Library> searchLibraries(String query) {
        List<Library> libs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = LIB_NAME + " LIKE ? OR " + LIB_TAGS + " LIKE ?";
        String[] args = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_LIBRARIES, null, selection, args, null, null, LIB_CREATED_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Library lib = new Library(
                        cursor.getLong(cursor.getColumnIndexOrThrow(LIB_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LIB_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LIB_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LIB_TAGS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(LIB_CREATED_DATE))
                );
                libs.add(lib);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return libs;
    }
}
