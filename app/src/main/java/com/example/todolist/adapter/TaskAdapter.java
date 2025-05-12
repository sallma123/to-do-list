package com.example.todolist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.data.dao.TaskDao;
import com.example.todolist.data.model.Task;
import com.example.todolist.db.AppDatabase;

import java.util.List;

/**
  Adapter personnalisé pour l'affichage d'une liste de tâches avec des en-têtes.
  Permet de gérer les interactions utilisateur : suppression, clic, et mise à jour de l'état.
 */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Types de vues : en-tête ou tâche
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    private final List<Object> taskList;
    private final OnTaskDeleteListener deleteListener;
    private final OnTaskClickListener clickListener;
    private final OnTaskStatusChangeListener statusChangeListener;
    private final TaskDao taskDao;

    // Interfaces pour la communication avec l'activité
    public interface OnTaskStatusChangeListener {
        void onStatusChanged();
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(Context context, List<Object> taskList,
                       OnTaskDeleteListener deleteListener,
                       OnTaskClickListener clickListener,
                       OnTaskStatusChangeListener statusChangeListener) {
        this.taskList = taskList;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
        this.statusChangeListener = statusChangeListener;
        this.taskDao = AppDatabase.getInstance(context).taskDao();
    }

    // Détermine le type de vue à afficher à une position donnée
    @Override
    public int getItemViewType(int position) {
        return taskList.get(position) instanceof String ? TYPE_HEADER : TYPE_TASK;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            // Affichage du titre de section (ex: "Previous", "Future", "Completed")
            ((HeaderViewHolder) holder).headerText.setText((String) taskList.get(position));
        } else if (holder instanceof TaskViewHolder) {
            Task task = (Task) taskList.get(position);
            TaskViewHolder th = (TaskViewHolder) holder;

            // Empêche la réexécution du listener lors du recyclage
            th.checkBox.setOnCheckedChangeListener(null);

            th.title.setText(task.getTitle());
            th.checkBox.setChecked(task.isDone);

            // Suppression de la tâche
            th.deleteIcon.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onTaskDelete(task);
            });

            // Navigation vers les détails de la tâche
            th.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onTaskClick(task);
            });

            // Coche / décoche la tâche
            th.checkBox.setOnCheckedChangeListener((button, isChecked) -> {
                task.isDone = isChecked;
                new Thread(() -> {
                    taskDao.update(task);
                    button.post(() -> {
                        if (statusChangeListener != null) {
                            statusChangeListener.onStatusChanged();
                        }
                    });
                }).start();
            });
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // ViewHolder pour les tâches
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView deleteIcon;
        CheckBox checkBox;

        public TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            deleteIcon = itemView.findViewById(R.id.deleteTask);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }

    // ViewHolder pour les titres de section
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.sectionTitle);
        }
    }
}
