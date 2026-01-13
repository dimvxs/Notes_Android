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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private final List<Note> notes = new ArrayList<>();
    private FloatingActionButton fabAdd;

    private final ActivityResultLauncher<Intent> noteDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            boolean changed = result.getData().getBooleanExtra("noteChanged", false);
                            boolean deleted = result.getData().getBooleanExtra("noteDeleted", false);
                            if (changed || deleted) {
                                loadNotesFromDb();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Светлая");
        if ("Тёмная".equals(theme)) setTheme(R.style.Theme_Dark);
        else if ("Синяя".equals(theme)) setTheme(R.style.Theme_Blue);
        else setTheme(R.style.Theme_Light);

        Typeface typeface;
        try {
            String fontFile = prefs.getString("font", "roboto_variablefont.ttf");  // ← "font" вместо "fonts"
            typeface = Typeface.createFromAsset(getAssets(), "fonts/" + fontFile);
        } catch (Exception e) {
            typeface = Typeface.DEFAULT;
        }


        setContentView(R.layout.activity_main);

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Адаптер с двумя слушателями (клик и долгое нажатие)
        adapter = new NoteAdapter(notes, this::openNoteDetail,
                this::showDeleteNoteDialog);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddNoteDialog());

        loadNotesFromDb();
    }

    private void loadNotesFromDb() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            NoteDao noteDao = db.noteDao();
            List<Note> noteList = noteDao.getAll();

            if (noteList.isEmpty()) {
                Note demo = new Note();
                demo.title = "Добро пожаловать!";
                demo.content = "Это пример заметки.\nНажми ➕ чтобы создать свою.";
                demo.important = true;
                noteDao.insert(demo);
                noteList = noteDao.getAll();
            }

            List<Note> finalNoteList = noteList;
            runOnUiThread(() -> {
                notes.clear();
                notes.addAll(finalNoteList);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

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

    private void openNoteDetail(Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("noteId", note.id);
        noteDetailLauncher.launch(intent);
    }


private void showDeleteNoteDialog(Note note) {
    new AlertDialog.Builder(this)
            .setTitle("Удалить заметку")
            .setMessage("Вы действительно хотите удалить эту заметку?")
            .setPositiveButton("Удалить", (d, w) -> {
                // 1. Удаляем из базы в фоне
                new Thread(() -> {
                    AppDatabase.getInstance(this).noteDao().delete(note);
                }).start();

                // 2. Удаляем сразу из списка в памяти (это и есть "реальное время")
                int position = notes.indexOf(note);
                if (position != -1) {
                    notes.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, notes.size()); // чтобы не было пустых мест
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
}

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