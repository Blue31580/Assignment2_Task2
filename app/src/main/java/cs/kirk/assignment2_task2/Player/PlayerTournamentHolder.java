package cs.kirk.assignment2_task2.Player;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cs.kirk.assignment2_task2.R;

public class PlayerTournamentHolder extends RecyclerView.ViewHolder {

    TextView tvPName, tvPInfo, tvPDates, tvPStatus, tvPScore;
    Button btnPlay;

    public PlayerTournamentHolder(@NonNull View itemView) {
        super(itemView);
        tvPName = itemView.findViewById(R.id.tvPName);
        tvPInfo = itemView.findViewById(R.id.tvPInfo);
        tvPDates = itemView.findViewById(R.id.tvPDates);
        tvPStatus = itemView.findViewById(R.id.tvPStatus);
        tvPScore = itemView.findViewById(R.id.tvPScore);

        btnPlay = itemView.findViewById(R.id.btnPlay);
    }
}
