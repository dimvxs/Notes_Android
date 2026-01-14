package com.example.notes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.data.db.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    // Интерфейсы для обработки кликов
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note);
    }

    private final List<Note> notes;
    private final OnNoteClickListener clickListener;
    private final OnNoteLongClickListener longClickListener;

    public NoteAdapter(
            @NonNull List<Note> notes,
            OnNoteClickListener clickListener,
            OnNoteLongClickListener longClickListener) {
        this.notes = notes;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.textTitle.setText(note.title != null ? note.title : "");
        holder.textContent.setText(note.content != null ? note.content : "");

        // Цвет карточки в зависимости от важности
        int backgroundColor;
        if (note.important) {
            backgroundColor = holder.itemView.getResources().getColor(R.color.yellow);
        } else {
            backgroundColor = holder.itemView.getResources().getColor(android.R.color.white);
        }
        holder.cardView.setCardBackgroundColor(backgroundColor);

        // Обработка кликов
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onNoteLongClick(note);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    // ViewHolder
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textContent;
        MaterialCardView cardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textContent = itemView.findViewById(R.id.textContent);
            cardView = itemView.findViewById(R.id.cardNote);
        }
    }
}