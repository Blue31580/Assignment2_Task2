package cs.kirk.assignment2_task2.Player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs.kirk.assignment2_task2.Login;
import cs.kirk.assignment2_task2.Quiz.Tournament;
import cs.kirk.assignment2_task2.R;

public class PlayerDashboard extends AppCompatActivity {

    private Button btnOnGoing, btnUpcoming, btnPast, btnParticipated, btnLogout;
    private TextView tvWelcome, tvListTitle;
    private RecyclerView rvPlayer;
    private PlayerTournamentAdapter adapter;

    private final List<Tournament> allTournaments = new ArrayList<>();
    private final List<Tournament> shown = new ArrayList<>();
    private final Map<String, Long> myScores = new HashMap<>();

    private String filter = "ongoing";
    private String uid;

    private DatabaseReference tournamentsRef, resultRef;
    private ValueEventListener tournamentsListener, resultsListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(PlayerDashboard.this, Login.class));
            finish();
            return;
        }
        uid = user.getUid();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvListTitle = findViewById(R.id.tvListTitle);
        rvPlayer = findViewById(R.id.rvPlayer);
        btnOnGoing = findViewById(R.id.btnOngoing);
        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnPast = findViewById(R.id.btnPast);
        btnParticipated = findViewById(R.id.btnParticipated);
        btnLogout = findViewById(R.id.btnLogout);

        adapter = new PlayerTournamentAdapter(shown, myScores,
                new PlayerTournamentAdapter.OnPlayListener() {
                    @Override
                    public void onPlay(Tournament tournament) {
                        openQuiz(tournament);
                    }
                });
        rvPlayer.setLayoutManager(new LinearLayoutManager(this));
        rvPlayer.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("users").child(uid).child("name").get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getValue(String.class);
                    if (name != null) {
                        tvWelcome.setText("Welcome, " + name);
                    }
                });

        tournamentsRef = FirebaseDatabase.getInstance().getReference("tournaments");
        resultRef = FirebaseDatabase.getInstance().getReference("results");
        listenForTournaments();
        listenForMyResults();

        btnOnGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "ongoing";
                applyFilter();
            }
        });

        btnUpcoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "upcoming";
                applyFilter();
            }
        });

        btnPast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "past";
                applyFilter();
            }
        });

        btnParticipated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = "participated";
                applyFilter();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(PlayerDashboard.this, Login.class));
                finish();
            }
        });
    }

    private void listenForTournaments() {
        tournamentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTournaments.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Tournament t = child.getValue(Tournament.class);
                    if (t != null) {
                        t.setId(child.getKey());
                        allTournaments.add(t);
                    }
                }
                Collections.sort(allTournaments, (a, b) -> Long.compare(a.getStartDate(), b.getStartDate()));
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlayerDashboard.this, "Load Failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
        tournamentsRef.addValueEventListener(tournamentsListener);
    }

    private void listenForMyResults() {
        resultsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myScores.clear();
                for (DataSnapshot tournament : snapshot.getChildren()) {
                    DataSnapshot mine = tournament.child(uid);
                    if (mine.exists()) {
                        Long score = mine.child("score").getValue(Long.class);
                        myScores.put(tournament.getKey(), score != null ? score : 0L);
                    }
                }
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlayerDashboard.this, "Load Failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
        resultRef.addValueEventListener(resultsListener);
    }

    private void applyFilter() {
        shown.clear();
        long now = System.currentTimeMillis();

        for (Tournament t : allTournaments) {
            boolean participated = myScores.containsKey(t.getId());
            boolean include;
            switch (filter) {
                case "participated":
                    include = participated;
                    break;
                case "ongoing":
                    include = !participated && now >= t.getStartDate() && now <= t.getEndDate();
                    break;
                case "upcoming":
                    include = !participated && now < t.getStartDate();
                    break;
                default:
                    include = !participated && now > t.getEndDate();
                    break;
            }
            if (include) {
                shown.add(t);
            }
        }

        tvListTitle.setText(titleFor(filter) + " (" + shown.size() + ")");
        adapter.notifyDataSetChanged();
    }

    private String titleFor(String f) {
        if ("participated".equals(f)) return "Participated";
        if ("upcoming".equals(f)) return "Upcoming";
        if ("past".equals(f)) return "Past";
        return "Ongoing";
    }
    private void openQuiz(Tournament t) {
        long now = System.currentTimeMillis();
        boolean ongoing = now >= t.getStartDate() && now <= t.getEndDate();
        if (myScores.containsKey(t.getId()) || !ongoing) {
            Toast.makeText(this, "You can't play this tournament", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(PlayerDashboard.this, QuizActivity.class);
        intent.putExtra("tournamentId", t.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tournamentsListener != null) {
            tournamentsRef.removeEventListener(tournamentsListener);
        }
        if (resultsListener != null) {
            resultRef.removeEventListener(resultsListener);
        }
    }
}