//package com.example.notes.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.notes.R;
//import com.example.notes.data.db.Note;
//import com.google.android.material.card.MaterialCardView;
//
//import java.util.List;
//
//public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
//
//
//    public interface OnItemClickListener {
//        void onItemClick(Note note);
//        void onItemLongClick(Note note);
//    }
//
//    private List<Note> noteList;
//    private OnItemClickListener listener;
//
//    public NoteAdapter(List<Note> noteList) {
//        this.noteList = noteList;
//    }
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        this.listener = listener;
//    }
//
//
//    public void setNotes(List<Note> notes) {
//        this.noteList = notes;
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getItemCount() {
//        return noteList != null ? noteList.size() : 0;
//    }
//
//
//    @Override
//    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
//        Note note = noteList.get(position);
//        holder.textTitle.setText(note.title);
//        holder.textContent.setText(note.content);
//
//        // Цвет карточки для важных заметок
//        if (note.important) {
//            holder.cardView.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.yellow));
//        } else {
//            holder.cardView.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.white));
//        }
//
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) listener.onItemClick(note);
//        });
//
//        holder.itemView.setOnLongClickListener(v -> {
//            if (listener != null) listener.onItemLongClick(note);
//            return true;
//        });
//    }
//
//
//
//
//    static class NoteViewHolder extends RecyclerView.ViewHolder {
//        TextView textTitle, textContent;
//        MaterialCardView cardView;
//
//        public NoteViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textTitle = itemView.findViewById(R.id.textTitle);
//            textContent = itemView.findViewById(R.id.textContent);
//            cardView = (MaterialCardView) itemView;
//        }
//    }
//}
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