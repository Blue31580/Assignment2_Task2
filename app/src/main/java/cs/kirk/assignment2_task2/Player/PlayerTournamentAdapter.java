package cs.kirk.assignment2_task2.Player;

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

public class PlayerTournamentAdapter extends RecyclerView.Adapter<PlayerTournamentHolder> {

    public interface OnPlayListener {
        void onPlay(Tournament tournament);
    }

    private final List<Tournament> tournaments;
    private final Map<String, Long> myScores;
    private final OnPlayListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PlayerTournamentAdapter(List<Tournament> tournaments, Map<String, Long> myScores,
                                   OnPlayListener listener) {
        this.tournaments = tournaments;
        this.myScores = myScores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerTournamentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_tournament, parent, false);
        return new PlayerTournamentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerTournamentHolder holder, int position) {
        Tournament t = tournaments.get(position);

        holder.tvPName.setText(t.getName());
        holder.tvPInfo.setText("Category: " + t.getCategory()
                + "\nDifficulty: " + capitalise(t.getDifficulty())
                + " | Type: " + typeLabel(t.getType()));

        String start = dateFormat.format(new Date(t.getStartDate()));
        String end = dateFormat.format(new Date(t.getEndDate()));
        holder.tvPDates.setText("Start: " + start + "   End: " + end);

        long now = System.currentTimeMillis();
        boolean participated = myScores.containsKey(t.getId());

        if (participated) {
            holder.tvPStatus.setText("Status: Participated");
            holder.tvPScore.setVisibility(View.VISIBLE);
            holder.tvPScore.setText("Your score: " + myScores.get(t.getId()) + " / 10");
            holder.btnPlay.setVisibility(View.GONE);

        } else if (now < t.getStartDate()) {
            holder.tvPStatus.setText("Status: Upcoming (starts " + start + ")");
            holder.tvPScore.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);

        } else if (now > t.getEndDate()) {
            holder.tvPStatus.setText("Status: Past (ended " + end + ")");
            holder.tvPScore.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);

        } else {
            holder.tvPStatus.setText("Status: Ongoing (ends " + end + ")");
            holder.tvPScore.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.VISIBLE);
        }

        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlay(t);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tournaments.size();
    }

    private String capitalise(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String typeLabel(String type) {
        if ("multiple".equals(type)) return "Multiple Choice";
        if ("boolean".equals(type)) return "True / False";
        return "Mixed";
    }
}