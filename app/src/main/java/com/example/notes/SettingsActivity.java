package com.example.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Активность настроек приложения.
 * Позволяет выбрать шрифт и тему оформления.
 * Сохраняет настройки в SharedPreferences и перезапускает MainActivity для применения изменений.
 */
public class SettingsActivity extends AppCompatActivity {

    // Элементы интерфейса
    private Spinner spinnerFont;     // Выбор шрифта
    private Spinner spinnerTheme;    // Выбор темы
    private Button btnSave;          // Кнопка сохранения настроек

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // === Инициализация view ===
        spinnerFont = findViewById(R.id.spinnerFont);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        btnSave = findViewById(R.id.btnSaveSettings);

        // === Настройка спиннера шрифтов ===
        // Список доступных шрифтов (имена файлов из assets/fonts/)
        String[] fonts = {
                "roboto_variablefont.ttf",
                "roboto_italic_variablefont.ttf",
                "chokokutai_regular.ttf",
                "bitcountpropsingle_variablefont.ttf"
        };
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                fonts
        );
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(fontAdapter);

        // === Настройка спиннера тем ===
        String[] themes = {"Светлая", "Тёмная", "Синяя"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                themes
        );
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);

        // === Загрузка сохранённых настроек ===
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String savedFont = prefs.getString("font", "roboto_variablefont.ttf");   // дефолтный шрифт
        String savedTheme = prefs.getString("theme", "Светлая");                 // дефолтная тема

        // Устанавливаем текущие значения в спиннеры
        spinnerFont.setSelection(fontAdapter.getPosition(savedFont));
        spinnerTheme.setSelection(themeAdapter.getPosition(savedTheme));

        // === Слушатель кнопки "Сохранить" ===
        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();

            // Сохраняем выбранный шрифт (имя файла)
            editor.putString("font", (String) spinnerFont.getSelectedItem());

            // Сохраняем выбранную тему
            editor.putString("theme", (String) spinnerTheme.getSelectedItem());

            editor.apply();  // Асинхронное сохранение

            // Перезапускаем MainActivity, чтобы настройки применились
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Закрываем текущую активность
            finish();
        });
    }
}