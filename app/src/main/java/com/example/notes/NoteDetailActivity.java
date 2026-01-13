package com.example.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.data.db.AppDatabase;
import com.example.notes.data.db.Note;
import com.example.notes.data.db.NoteDao;

/**
 * Активность для просмотра и редактирования одной заметки.
 * Отображает заголовок, содержание, чекбокс "Важно", кнопки "Сохранить" и "Удалить".
 * Поддерживает темы и кастомные шрифты из настроек.
 */
public class NoteDetailActivity extends AppCompatActivity {

    // Поля ввода и элементы управления
    private EditText editTitle, editContent;
    private CheckBox importantCheck;
    private Button btnSave, btnDelete;

    // Текущая заметка (загружается из БД)
    private Note note;
    private long noteId = -1;  // ID заметки из Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // === Применяем выбранную пользователем тему ===
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Светлая");
        if ("Тёмная".equals(theme)) {
            setTheme(R.style.Theme_Dark);
        } else if ("Синяя".equals(theme)) {
            setTheme(R.style.Theme_Blue);
        } else {
            setTheme(R.style.Theme_Light);
        }

        // === Загружаем выбранный шрифт из assets ===
        Typeface typeface;
        try {
            String fontFile = prefs.getString("font", "roboto_variablefont.ttf");
            typeface = Typeface.createFromAsset(getAssets(), "fonts/" + fontFile);
        } catch (Exception e) {
            // Если шрифт не найден — используем стандартный
            typeface = Typeface.DEFAULT;
        }

        // Устанавливаем layout экрана
        setContentView(R.layout.activity_note_detail);

        // === Инициализация всех view ===
        editTitle = findViewById(R.id.editTitleDetail);
        editContent = findViewById(R.id.editContentDetail);
        importantCheck = findViewById(R.id.checkImportant);
        btnSave = findViewById(R.id.btnSaveNote);
        btnDelete = findViewById(R.id.btnDeleteNote);

        // Применяем шрифт к полям ввода
        editTitle.setTypeface(typeface);
        editContent.setTypeface(typeface);

        // === Изменение фона заголовка при отметке "Важно" ===
        importantCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTitle.setBackgroundColor(getResources().getColor(R.color.yellow));
            } else {
                editTitle.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        });

        // Получаем ID заметки из Intent
        noteId = getIntent().getLongExtra("noteId", -1);
        if (noteId == -1) {
            Toast.makeText(this, "Ошибка: ID заметки не передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Загружаем заметку из базы
        loadNoteFromDatabase();

        // === Слушатели кнопок ===
        btnSave.setOnClickListener(v -> saveNote());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    /**
     * Загружает заметку по ID из базы данных в фоновом потоке
     * и заполняет поля на UI-потоке.
     */
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

                    // Применяем цвет "Важно" сразу после загрузки
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

    /**
     * Сохраняет изменения заметки в базу данных.
     * Возвращает результат в MainActivity (noteChanged = true).
     */
    private void saveNote() {
        if (note == null) return;

        // Получаем новые значения из полей
        String newTitle = editTitle.getText().toString().trim();
        String newContent = editContent.getText().toString().trim();
        boolean newImportant = importantCheck.isChecked();

        // Обновляем объект заметки
        note.title = newTitle;
        note.content = newContent;
        note.important = newImportant;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.noteDao().update(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();

                // Сообщаем MainActivity, что заметка изменена
                Intent result = new Intent();
                result.putExtra("noteChanged", true);
                setResult(RESULT_OK, result);
                finish();
            });
        }).start();
    }

    /**
     * Показывает диалог подтверждения удаления заметки.
     */
    private void showDeleteConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Удалить заметку")
                .setMessage("Вы действительно хотите удалить эту заметку?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteNote())
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * Удаляет заметку из базы и возвращает результат в MainActivity.
     */
    private void deleteNote() {
        if (note == null) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.noteDao().delete(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();

                // Сообщаем MainActivity, что заметка удалена
                Intent result = new Intent();
                result.putExtra("noteDeleted", true);
                setResult(RESULT_OK, result);
                finish();
            });
        }).start();
    }
}