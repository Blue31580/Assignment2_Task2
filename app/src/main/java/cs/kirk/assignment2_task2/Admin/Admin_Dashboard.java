package cs.kirk.assignment2_task2.Admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cs.kirk.assignment2_task2.Login;
import cs.kirk.assignment2_task2.Quiz.CreateTournament;
import cs.kirk.assignment2_task2.Quiz.EditTournament;
import cs.kirk.assignment2_task2.Quiz.Tournament;
import cs.kirk.assignment2_task2.Quiz.TournamentAdapter;
import cs.kirk.assignment2_task2.R;

public class Admin_Dashboard extends AppCompatActivity {

    private Button btnLogout, btnCreate;
    private RecyclerView rvTournament;
    private TournamentAdapter adapter;

    private final List<Tournament> tournaments = new ArrayList<>();
    private final Map<String, String> playerRatings = new HashMap<>();

    private DatabaseReference tournamentsRef, resultsRef;
    private ValueEventListener tournamentsListener, resultsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        final int pad = (int) (16 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + pad, systemBars.top + pad,
                    systemBars.right + pad, systemBars.bottom + pad);
            return insets;
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnCreate = findViewById(R.id.btnCreate);
        rvTournament = findViewById(R.id.rvTournament);

        adapter = new TournamentAdapter(tournaments, playerRatings,
                new TournamentAdapter.OnTournamentActionListener() {
                    @Override
                    public void onEdit(Tournament tournament) {
                        openEdit(tournament);
                    }

                    @Override
                    public void onDelete(Tournament tournament) {
                        confirmDelete(tournament);
                    }
                });
        rvTournament.setLayoutManager(new LinearLayoutManager(this));
        rvTournament.setAdapter(adapter);

        tournamentsRef = FirebaseDatabase.getInstance().getReference("tournaments");
        resultsRef = FirebaseDatabase.getInstance().getReference("results");
        listenForTournaments();
        listenForRatings();

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Admin_Dashboard.this, CreateTournament.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Admin_Dashboard.this, Login.class));
                finish();
            }
        });
    }

    private void listenForTournaments() {
        tournamentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tournaments.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Tournament t = child.getValue(Tournament.class);
                    if (t != null) {
                        t.setId(child.getKey());
                        tournaments.add(t);
                    }
                }
                Collections.sort(tournaments, (a, b) -> Long.compare(a.getStartDate(), b.getStartDate()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Dashboard.this, "Load Failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
        tournamentsRef.addValueEventListener(tournamentsListener);
    }

    private void listenForRatings() {
        resultsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playerRatings.clear();
                for (DataSnapshot tournament : snapshot.getChildren()) {
                    double sum = 0;
                    int count = 0;
                    for (DataSnapshot player : tournament.getChildren()) {
                        Long rating = player.child("rating").getValue(Long.class);
                        if (rating != null) {
                            sum += rating;
                            count++;
                        }
                    }
                    if (count > 0) {
                        playerRatings.put(tournament.getKey(), String.format(Locale.getDefault(),
                                "Players' rating: %.1f / 5 (%d ratings)", sum / count, count));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Dashboard.this, "Load Failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
        resultsRef.addValueEventListener(resultsListener);
    }

    private void openEdit(Tournament t) {
        Intent intent = new Intent(Admin_Dashboard.this, EditTournament.class);
        intent.putExtra("id", t.getId());
        intent.putExtra("name", t.getName());
        intent.putExtra("start", t.getStartDate());
        intent.putExtra("end", t.getEndDate());
        startActivity(intent);
    }

    private void confirmDelete(Tournament t) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tournament")
                .setMessage("Are you sure you want to delete \"" + t.getName() + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tournamentsRef.child(t.getId()).removeValue()
                                .addOnSuccessListener(unused -> {
                                    resultsRef.child(t.getId()).removeValue();
                                    Toast.makeText(Admin_Dashboard.this, "Tournament Deleted",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(Admin_Dashboard.this,
                                        "Delete Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tournamentsListener != null) {
            tournamentsRef.removeEventListener(tournamentsListener);
        }
        if (resultsListener != null) {
            resultsRef.removeEventListener(resultsListener);
        }
    }
}