package com.example.todolist.utils;

import android.view.View;
import android.widget.TextView;

import com.example.todolist.R;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.ViewContainer;

/**
 * Conteneur personnalisé pour chaque jour dans le calendrier.
 * Utilisé avec la librairie Kizitonwose CalendarView.
 */
public class DayViewContainer extends ViewContainer {

    public final TextView dayText; // Texte affichant le jour (numéro)
    public final View dotView;     // Vue représentant un point sous le jour (ex : pour marquer une tâche)

    private CalendarDay currentDay;

    /**
     Constructeur appelé à chaque création de jour dans la vue calendrier.
     @param view Vue du jour gonflée depuis XML
     @param listener Callback pour les clics sur un jour
     */
    public DayViewContainer(View view, OnDayClickListener listener) {
        super(view);
        dayText = view.findViewById(R.id.dayText);
        dotView = view.findViewById(R.id.dot);

        // Gestion du clic sur le jour sélectionné
        view.setOnClickListener(v -> {
            if (currentDay != null && currentDay.getPosition() == DayPosition.MonthDate) {
                listener.onDayClicked(currentDay);
            }
        });
    }

    /**
     Associe un jour du calendrier à ce conteneur (appelé automatiquement par le binder).
     @param day Instance de CalendarDay représentant le jour actuel
     */
    public void bind(CalendarDay day) {
        this.currentDay = day;

        // Affiche uniquement le numéro du jour
        dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

        // Grise les jours en dehors du mois actuel
        dayText.setAlpha(day.getPosition() == DayPosition.MonthDate ? 1f : 0.3f);

        // Supprime le fond actif (il sera défini ailleurs)
        dayText.setBackground(null);

        // Le point est géré par CalendarActivity (selon la présence de tâches)
        dotView.setVisibility(View.GONE);
    }

    //Interface pour détecter les clics sur un jour du calendrier.
    public interface OnDayClickListener {
        void onDayClicked(CalendarDay day);
    }
}
