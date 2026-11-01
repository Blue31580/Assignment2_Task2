package cs.kirk.assignment2_task2.Admin;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cs.kirk.assignment2_task2.R;

public class TournamentHolder extends RecyclerView.ViewHolder {
    TextView tvTourName, tvTourInfo, tvTourDates, tvTourStatus, tvTourRating, tvTourPlayerRating;
    Button btnEditTour, btnDeleteTour;

    public TournamentHolder(@NonNull View itemView) {
        super(itemView);

        tvTourName = itemView.findViewById(R.id.tvTourName);
        tvTourInfo = itemView.findViewById(R.id.tvTourInfo);
        tvTourDates = itemView.findViewById(R.id.tvTourDates);
        tvTourStatus = itemView.findViewById(R.id.tvTourStatus);
        tvTourRating = itemView.findViewById(R.id.tvTourRating);
        tvTourPlayerRating = itemView.findViewById(R.id.tvTourPlayerRating);

        btnEditTour = itemView.findViewById(R.id.btnEditTour);
        btnDeleteTour = itemView.findViewById(R.id.btnDeleteTour);
    }
}
