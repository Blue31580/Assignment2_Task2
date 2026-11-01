package cs.kirk.assignment2_task2.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cs.kirk.assignment2_task2.Quiz.Tournament;
import cs.kirk.assignment2_task2.R;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentHolder> {

    private final List<Tournament> tournaments;
    private final Map<String, String> playerRatings;
    private final OnTournamentActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnTournamentActionListener {
        void onEdit(Tournament tournament);

        void onDelete(Tournament tournament);
    }

    public TournamentAdapter(List<Tournament> tournaments, Map<String, String> playerRatings, OnTournamentActionListener listener) {
        this.tournaments = tournaments;
        this.playerRatings = playerRatings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TournamentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tournament, parent, false);
        return new TournamentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TournamentHolder holder, int position) {
        Tournament t = tournaments.get(position);

        holder.tvTourName.setText(t.getName());
        holder.tvTourInfo.setText("category: " + t.getCategory() + "\nDifficulty: " + capitalise(t.getDifficulty())
                + " | Type: " + typeLabel(t.getType()));
        holder.tvTourDates.setText("Start: " + dateFormat.format(new Date(t.getStartDate()))
                + " End: " + dateFormat.format(new Date(t.getEndDate())));

        long now = System.currentTimeMillis();
        String status;
        if(now < t.getStartDate()) {
            status = "Upcoming";
        } else if (now > t.getEndDate()) {
            status = "Past";
        } else {
            status = "Ongoing";
        }

        holder.tvTourStatus.setText("Status: " + status);
        holder.tvTourRating.setText("Rating: " + t.getRating() + " / 5");

        String players = playerRatings.get(t.getId());
        holder.tvTourPlayerRating.setText(players != null ? players : "Player's rating: None");

        holder.btnEditTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEdit(t);
            }
        });

        holder.btnDeleteTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(t);
            }
        });

    }

    @Override
    public int getItemCount() {
        return tournaments.size();
    }

    private String capitalise(String text) {
        if(text == null || text.isEmpty() ) return"";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String typeLabel(String type) {
        if("multiple".equals(type)) return "Multiple Choice";
        if("boolean".equals(type)) return "True or False";
        return "Mixed";
    }

}
