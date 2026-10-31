package cs.kirk.assignment2_task2.Quiz;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cs.kirk.assignment2_task2.Quiz.Category;
import cs.kirk.assignment2_task2.Quiz.CategoryController;
import cs.kirk.assignment2_task2.Quiz.Question;
import cs.kirk.assignment2_task2.Quiz.QuizController;
import cs.kirk.assignment2_task2.Quiz.Tournament;
import cs.kirk.assignment2_task2.R;

public class CreateTournament extends AppCompatActivity
        implements QuizController.QuizListener, CategoryController.CategoryListener {

    private EditText etName, etStartDate, etEndDate;
    private Spinner spCategory, spDifficulty, spType;
    private RatingBar rbRating;
    private Button btnCreate;

    // categories come from the OpenTDB API
    private final List<Category> categories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    private final String[] difficultyNames = {"Easy", "Medium", "Hard"};
    private final String[] typeNames = {"Mixed (Multiple Choice + True/False)",
            "Multiple Choice", "True / False"};

    private long startMillis = 0;
    private long endMillis = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // the tournament being built while its questions are fetched
    private Tournament pending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_tournament);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        spCategory = findViewById(R.id.spCategory);
        spDifficulty = findViewById(R.id.spDifficulty);
        spType = findViewById(R.id.spType);
        rbRating = findViewById(R.id.rbRating);
        btnCreate = findViewById(R.id.btnCreate);

        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>());
        spCategory.setAdapter(categoryAdapter);
        spDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, difficultyNames));
        spType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, typeNames));

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(true);
            }
        });

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(false);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTournament();
            }
        });

        // Create stays off until the categories arrive from OpenTDB
        btnCreate.setEnabled(false);
        CategoryController categoryController = new CategoryController(this);
        categoryController.start();
    }

    @Override
    public void onCategoriesLoaded(List<Category> list) {
        categories.clear();
        categories.addAll(list);
        categoryAdapter.clear();
        for (Category c : list) {
            categoryAdapter.add(c.getName());
        }
        btnCreate.setEnabled(true);
    }

    @Override
    public void onCategoriesError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Start date is saved as the start of that day, end date as the end of that day
    private void pickDate(boolean isStart) {
        Calendar today = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            if (isStart) {
                chosen.set(year, month, day, 0, 0, 0);
                chosen.set(Calendar.MILLISECOND, 0);
                startMillis = chosen.getTimeInMillis();
                etStartDate.setText(dateFormat.format(chosen.getTime()));
            } else {
                chosen.set(year, month, day, 23, 59, 59);
                chosen.set(Calendar.MILLISECOND, 999);
                endMillis = chosen.getTimeInMillis();
                etEndDate.setText(dateFormat.format(chosen.getTime()));
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void createTournament() {
        String name = etName.getText().toString().trim();
        int rating = (int) rbRating.getRating();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter a tournament name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startMillis == 0 || endMillis == 0) {
            Toast.makeText(this, "Pick a start and end date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endMillis < startMillis) {
            Toast.makeText(this, "End date must be on or after the start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating < 1) {
            Toast.makeText(this, "Choose a rating from 1 to 5", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryPos = spCategory.getSelectedItemPosition();
        if (categoryPos < 0 || categoryPos >= categories.size()) {
            Toast.makeText(this, "Categories are not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        Category chosenCategory = categories.get(categoryPos);
        String difficulty = difficultyNames[spDifficulty.getSelectedItemPosition()].toLowerCase();

        // null type = OpenTDB returns a mixture of multiple choice and true/false
        String type = "mixed";
        String apiType = null;
        int typePos = spType.getSelectedItemPosition();
        if (typePos == 1) {
            type = "multiple";
            apiType = "multiple";
        } else if (typePos == 2) {
            type = "boolean";
            apiType = "boolean";
        }

        pending = new Tournament();
        pending.setName(name);
        pending.setCategory(chosenCategory.getName());
        pending.setCategoryId(chosenCategory.getId());
        pending.setDifficulty(difficulty);
        pending.setType(type);
        pending.setStartDate(startMillis);
        pending.setEndDate(endMillis);
        pending.setRating(rating);

        // turned off until the questions arrive, so a double tap can't create two tournaments
        btnCreate.setEnabled(false);
        Toast.makeText(this, "Fetching questions...", Toast.LENGTH_SHORT).show();

        QuizController quiz = new QuizController(this);
        quiz.start(chosenCategory.getId(), difficulty, apiType);
    }

    @Override
    public void onQuestionsLoaded(List<Question> questions) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("tournaments");
        String key = ref.push().getKey();
        if (key == null) {
            btnCreate.setEnabled(true);
            return;
        }
        pending.setId(key);
        pending.setQuestions(questions);

        ref.child(key).setValue(pending)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Tournament created", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "Could not save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onError(String message) {
        btnCreate.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}