package com.example.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskDeleteListener deleteListener;
    private OnTaskClickListener clickListener;
    private TaskDao taskDao;

    // Interfaces
    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskDeleteListener deleteListener, OnTaskClickListener clickListener) {
        this.taskList = taskList;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
        this.taskDao = AppDatabase.getInstance(null).taskDao(); // si besoin : passe le Context dans le constructeur
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.checkBox.setChecked(task.isDone);

        // Suppression
        holder.deleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTaskDelete(task);
            }
        });

        // Navigation vers détail
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClick(task);
            }
        });

        // Changement d’état de la CheckBox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isDone = isChecked;
            new Thread(() -> taskDao.update(task)).start();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

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
}
