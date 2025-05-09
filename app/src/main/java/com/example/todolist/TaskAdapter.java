package com.example.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;
    private final OnTaskStatusChangeListener statusChangeListener;
    private final List<Object> taskList;
    private final OnTaskDeleteListener deleteListener;
    private final OnTaskClickListener clickListener;
    private final TaskDao taskDao;

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
            ((HeaderViewHolder) holder).headerText.setText((String) taskList.get(position));
        } else if (holder instanceof TaskViewHolder) {
            Task task = (Task) taskList.get(position);
            TaskViewHolder th = (TaskViewHolder) holder;

            th.checkBox.setOnCheckedChangeListener(null); // Important: éviter les déclenchements multiples
            th.title.setText(task.getTitle());
            th.checkBox.setChecked(task.isDone);

            th.deleteIcon.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onTaskDelete(task);
            });

            th.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onTaskClick(task);
            });

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

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.sectionTitle);
        }
    }
}
