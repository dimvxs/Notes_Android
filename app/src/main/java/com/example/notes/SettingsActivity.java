package com.example.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerFont, spinnerTheme;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinnerFont = findViewById(R.id.spinnerFont);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        btnSave = findViewById(R.id.btnSaveSettings);

        // Шрифты
        String[] fonts = {"roboto_variablefont.ttf", "roboto_italic_variablefont.ttf",
                "chokokutai_regular.ttf", "bitcountpropsingle_variablefont.ttf"};
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fonts);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(fontAdapter);

        // Темы
        String[] themes = {"Светлая", "Тёмная", "Синяя"};
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, themes);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);

        // Загружаем сохраненные настройки
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        spinnerFont.setSelection(fontAdapter.getPosition(prefs.getString("font", "roboto_variablefont.ttf")));
        spinnerTheme.setSelection(themeAdapter.getPosition(prefs.getString("theme", "Светлая")));

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("app_settings", MODE_PRIVATE).edit();
            editor.putString("font", (String) spinnerFont.getSelectedItem());
            editor.putString("theme", (String) spinnerTheme.getSelectedItem());
            editor.apply();

            // Перезапускаем MainActivity, чтобы применились новые настройки
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

    }
}
