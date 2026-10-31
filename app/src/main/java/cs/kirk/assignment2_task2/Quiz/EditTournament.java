package cs.kirk.assignment2_task2.Quiz;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cs.kirk.assignment2_task2.R;

public class EditTournament extends AppCompatActivity {

    private EditText etName, etStartDate, etEndDate;
    private Button btnSave;

    private String tournamentId;
    private long startMillis, endMillis;
    private final SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_tournament);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnSave = findViewById(R.id.btnSave);

        tournamentId = getIntent().getStringExtra("id");
        startMillis = getIntent().getLongExtra("Start", 0);
        endMillis = getIntent().getLongExtra("End", 0);

        etName.setText(getIntent().getStringExtra("Name"));
        etStartDate.setText(dateformat.format(new Date(startMillis)));
        etEndDate.setText(dateformat.format(new Date(endMillis)));

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pickDate(true);
            }
        });

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pickDate(false);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveChanges();
            }
        });
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();

        if(name.isEmpty()) {
            Toast.makeText(this, "Enter a Tournament Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(endMillis < startMillis) {
            Toast.makeText(this, "Incorrect Input of Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if(tournamentId == null) {
            Toast.makeText(this, "Tournament Not Found", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("Name", name);
        data.put("StartDate", startMillis);
        data.put("EndDate", endMillis);

        FirebaseDatabase.getInstance().getReference("tournament").child(tournamentId)
                .updateChildren(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Tournament Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Update Failed" + e.getMessage(), Toast.LENGTH_LONG).show());

    }

    private void pickDate(boolean isStart) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(isStart ? startMillis : endMillis);
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            if(isStart) {
                chosen.set(year, month, day, 0, 0 ,0);
                chosen.set(Calendar.MILLISECOND, 0);
                startMillis = chosen.getTimeInMillis();
                etStartDate.setText(dateformat.format(chosen.getTime()));
            } else {
                chosen.set(year, month, day, 23, 59, 59);
                chosen.set(Calendar.MILLISECOND, 999);
                endMillis = chosen.getTimeInMillis();
                etEndDate.setText(dateformat.format(chosen.getTime()));
            }
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
    }


}