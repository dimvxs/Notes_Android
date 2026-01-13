package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.data.db.AppDatabase;
import com.example.notes.data.db.Note;
import com.example.notes.data.db.NoteDao;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private CheckBox importantCheck;
    private Button btnSave, btnDelete;
    private Note note;
    private long noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        editTitle = findViewById(R.id.editTitleDetail);
        editContent = findViewById(R.id.editContentDetail);
        importantCheck = findViewById(R.id.checkImportant);
        btnSave = findViewById(R.id.btnSaveNote);
        btnDelete = findViewById(R.id.btnDeleteNote);

        // Изменение цвета заголовка при отметке "Важно"
        importantCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTitle.setBackgroundColor(getResources().getColor(R.color.yellow));
            } else {
                editTitle.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        });

        noteId = getIntent().getLongExtra("noteId", -1);
        if (noteId == -1) {
            Toast.makeText(this, "Ошибка: ID заметки не передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadNoteFromDatabase();

        btnSave.setOnClickListener(v -> saveNote());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadNoteFromDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            NoteDao noteDao = db.noteDao();
            note = noteDao.getById(noteId);

            runOnUiThread(() -> {
                if (note != null) {
                    editTitle.setText(note.title);
                    editContent.setText(note.content);
                    importantCheck.setChecked(note.important);

                    // Применяем цвет сразу после загрузки
                    if (note.important) {
                        editTitle.setBackgroundColor(getResources().getColor(R.color.yellow));
                    }
                } else {
                    Toast.makeText(this, "Заметка не найдена", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    private void saveNote() {
        if (note == null) return;

        String newTitle = editTitle.getText().toString().trim();
        String newContent = editContent.getText().toString().trim();
        boolean newImportant = importantCheck.isChecked();

        // Можно добавить проверку изменений, если хочешь
        // if (newTitle.equals(note.title) && newContent.equals(note.content) && newImportant == note.important) { finish(); return; }

        note.title = newTitle;
        note.content = newContent;
        note.important = newImportant;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.noteDao().update(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();

                Intent result = new Intent();
                result.putExtra("noteChanged", true);
                setResult(RESULT_OK, result);
                finish();
            });
        }).start();
    }

    private void showDeleteConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Удалить заметку")
                .setMessage("Вы действительно хотите удалить эту заметку?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteNote())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteNote() {
        if (note == null) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.noteDao().delete(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();

                Intent result = new Intent();
                result.putExtra("noteDeleted", true);
                setResult(RESULT_OK, result);
                finish();
            });
        }).start();
    }
}