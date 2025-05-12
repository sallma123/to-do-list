package com.example.todolist.utils;

import android.view.View;
import android.widget.TextView;

import com.example.todolist.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.ViewContainer;

public class DayViewContainer extends ViewContainer {

    public final TextView dayText;
    public final View dotView; // pour afficher le point

    private CalendarDay currentDay;

    public DayViewContainer(View view, OnDayClickListener listener) {
        super(view);
        dayText = view.findViewById(R.id.dayText);
        dotView = view.findViewById(R.id.dot); // ce View doit exister dans day_view.xml

        view.setOnClickListener(v -> {
            if (currentDay != null && currentDay.getPosition() == DayPosition.MonthDate) {
                listener.onDayClicked(currentDay);
            }
        });
    }

    public void bind(CalendarDay day) {
        this.currentDay = day;
        dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

        // Griser les jours hors mois
        dayText.setAlpha(day.getPosition() == DayPosition.MonthDate ? 1f : 0.3f);

        // Réinitialiser le fond
        dayText.setBackground(null);
        dotView.setVisibility(View.GONE); // sera géré ensuite dans CalendarActivity
    }

    public interface OnDayClickListener {
        void onDayClicked(CalendarDay day);
    }
}
