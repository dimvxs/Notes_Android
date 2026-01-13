package com.example.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.adapter.NoteAdapter;
import com.example.notes.data.db.AppDatabase;
import com.example.notes.data.db.Note;
import com.example.notes.data.db.NoteDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Главная активность приложения — экран со списком всех заметок.
 * Здесь отображается RecyclerView с заметками, FAB для добавления,
 * меню настроек и обработка перехода к деталям заметки.
 */
public class MainActivity extends AppCompatActivity {

    // Список заметок (хранится в памяти, синхронизируется с БД)
    private final List<Note> notes = new ArrayList<>();

    // RecyclerView и его адаптер
    private RecyclerView recyclerView;
    private NoteAdapter adapter;

    // Кнопка добавления новой заметки (плюсик)
    private FloatingActionButton fabAdd;

    // Launcher для запуска NoteDetailActivity и получения результата (изменено/удалено)
    private final ActivityResultLauncher<Intent> noteDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Проверяем, успешно ли завершена детальная активность
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            boolean changed = result.getData().getBooleanExtra("noteChanged", false);
                            boolean deleted = result.getData().getBooleanExtra("noteDeleted", false);
                            // Если заметка изменена или удалена — перезагружаем список
                            if (changed || deleted) {
                                loadNotesFromDb();
                            }
                        }
                    }
            );

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
            // Если шрифт не найден или ошибка — используем стандартный
            typeface = Typeface.DEFAULT;
        }

        // Устанавливаем layout экрана
        setContentView(R.layout.activity_main);

        // === Настройка Toolbar ===
        setSupportActionBar(findViewById(R.id.toolbar));

        // === Инициализация RecyclerView ===
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // === Создание и привязка адаптера заметок ===
        // Передаём список, обработчики клика и долгого нажатия
        adapter = new NoteAdapter(notes, this::openNoteDetail, this::showDeleteNoteDialog);
        recyclerView.setAdapter(adapter);

        // Применяем шрифт ко всему списку (если адаптер поддерживает)
        // adapter.setTypeface(typeface);  // ← раскомментируй, если добавил метод в NoteAdapter

        // === Кнопка добавления новой заметки ===
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddNoteDialog());

        // Загружаем заметки из базы при запуске
        loadNotesFromDb();
    }

    /**
     * Загружает все заметки из базы данных в фоновом потоке
     * и обновляет список на UI-потоке.
     */
    private void loadNotesFromDb() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            NoteDao noteDao = db.noteDao();
            List<Note> noteList = noteDao.getAll();

            // Если база пустая — создаём приветственную заметку
            if (noteList.isEmpty()) {
                Note demo = new Note();
                demo.title = "Добро пожаловать!";
                demo.content = "Это пример заметки.\nНажми ➕ чтобы создать свою.";
                demo.important = true;
                noteDao.insert(demo);
                noteList = noteDao.getAll();
            }

            // Передаём копию списка на UI-поток
            List<Note> finalNoteList = noteList;
            runOnUiThread(() -> {
                notes.clear();
                notes.addAll(finalNoteList);
                adapter.notifyDataSetChanged();  // Обновляем весь список
            });
        }).start();
    }

    /**
     * Показывает диалог для создания новой заметки.
     * После сохранения заметка добавляется в БД и список обновляется.
     */
    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новая заметка");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Заголовок");
        layout.addView(titleInput);

        EditText contentInput = new EditText(this);
        contentInput.setHint("Содержание");
        layout.addView(contentInput);

        builder.setView(layout);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (title.isEmpty() && content.isEmpty()) return;

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                Note note = new Note();
                note.title = title;
                note.content = content;
                note.important = false;
                db.noteDao().insert(note);
                runOnUiThread(this::loadNotesFromDb);
            }).start();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    /**
     * Открывает экран детального просмотра/редактирования заметки.
     * Передаёт ID заметки и ждёт результата через launcher.
     */
    private void openNoteDetail(Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("noteId", note.id);
        noteDetailLauncher.launch(intent);
    }

    /**
     * Показывает диалог подтверждения удаления заметки.
     * Удаляет из БД и сразу из списка в памяти (для мгновенного обновления UI).
     */
    private void showDeleteNoteDialog(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить заметку")
                .setMessage("Вы действительно хотите удалить эту заметку?")
                .setPositiveButton("Удалить", (d, w) -> {
                    // Удаляем из базы данных в фоновом потоке
                    new Thread(() -> {
                        AppDatabase.getInstance(this).noteDao().delete(note);
                    }).start();

                    // Мгновенно удаляем из списка и уведомляем адаптер
                    int position = notes.indexOf(note);
                    if (position != -1) {
                        notes.remove(position);
                        adapter.notifyItemRemoved(position);
                        // Обновляем позиции остальных элементов
                        adapter.notifyItemRangeChanged(position, notes.size());
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // === Меню в ActionBar ===
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}