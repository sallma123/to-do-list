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

    private final List<Object> itemList;
    private final OnTaskDeleteListener deleteListener;
    private final OnTaskClickListener clickListener;
    private final TaskDao taskDao;

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(Context context, List<Object> itemList,
                       OnTaskDeleteListener deleteListener,
                       OnTaskClickListener clickListener) {
        this.itemList = itemList;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
        this.taskDao = AppDatabase.getInstance(context).taskDao();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position) instanceof String ? TYPE_HEADER : TYPE_TASK;
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
            ((HeaderViewHolder) holder).headerText.setText((String) itemList.get(position));
        } else if (holder instanceof TaskViewHolder) {
            Task task = (Task) itemList.get(position);
            TaskViewHolder th = (TaskViewHolder) holder;
            th.title.setText(task.getTitle());
            th.checkBox.setChecked(task.isDone);

            th.deleteIcon.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onTaskDelete(task);
            });

            th.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onTaskClick(task);
            });

            th.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.isDone = isChecked;
                new Thread(() -> taskDao.update(task)).start();
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
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
