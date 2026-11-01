package cs.kirk.assignment2_task2.Player;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs.kirk.assignment2_task2.Quiz.Question;
import cs.kirk.assignment2_task2.Quiz.Tournament;
import cs.kirk.assignment2_task2.R;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuizTitle,  tvProgress, tvQuestion, tvFeedback, tvScore;
    private Button[] answerButtons;
    private Button btnNext, btnSubmitRating;
    private LinearLayout layoutQuestion, layoutResult;
    private RatingBar rbPlayerRating;

    private ColorStateList defaultTint;
    private DatabaseReference resultRef;

    private String tournamentId, uid;
    private List<Question> questions = new ArrayList<>();
    private int index = 0;
    private int score = 0;
    private boolean answered = false;

    private static final int GREEN = Color.parseColor("#466D55");
    private static final int RED = Color.parseColor("#C62828");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        tournamentId = getIntent().getStringExtra("tournamentId");
        if (user == null || tournamentId == null) {
            Toast.makeText(this, "Could not open the quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = user.getUid();

        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvScore = findViewById(R.id.tvScore);
        layoutQuestion = findViewById(R.id.llQuestion);
        layoutResult = findViewById(R.id.llResult);
        rbPlayerRating = findViewById(R.id.rbPlayerRating);
        btnNext = findViewById(R.id.btnNext);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);

        answerButtons = new Button[]{
                findViewById(R.id.btnAnswer1),
                findViewById(R.id.btnAnswer2),
                findViewById(R.id.btnAnswer3),
                findViewById(R.id.btnAnswer4)
        };
        defaultTint = answerButtons[0].getBackgroundTintList();

        resultRef = FirebaseDatabase.getInstance().getReference("results")
                .child(tournamentId).child(uid);

        View.OnClickListener answerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer((Button) v);
            }
        };
        for (Button b : answerButtons) {
            b.setOnClickListener(answerListener);
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                if (index < questions.size()) {
                    showQuestion();
                } else {
                    finishQuiz();
                }
            }
        });

        btnSubmitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRating();
            }
        });

        loadTournament();
    }

    // Checks the player hasn't played this one, that it is ongoing, then loads its 10 questions
    private void loadTournament() {
        resultRef.get().addOnSuccessListener(resultSnapshot -> {
            if (resultSnapshot.exists()) {
                Toast.makeText(this, "You have already played this tournament", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
                    .addOnSuccessListener(snapshot -> {
                        Tournament t = snapshot.getValue(Tournament.class);
                        if (t == null || t.getQuestions() == null || t.getQuestions().isEmpty()) {
                            Toast.makeText(this, "Tournament not found", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        long now = System.currentTimeMillis();
                        if (now < t.getStartDate() || now > t.getEndDate()) {
                            Toast.makeText(this, "This tournament is not ongoing", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        tvQuizTitle.setText(t.getName());
                        questions = t.getQuestions();
                        showQuestion();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Load Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Load Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        });
    }

    // One question per page
    private void showQuestion() {
        answered = false;
        Question q = questions.get(index);

        layoutQuestion.setVisibility(View.VISIBLE);
        tvProgress.setText("Question " + (index + 1) + " of " + questions.size());
        tvQuestion.setText(q.getQuestion());
        tvFeedback.setText("");
        btnNext.setVisibility(View.GONE);
        btnNext.setText(index == questions.size() - 1 ? "Finish" : "Next");

        // true/false keeps its order, multiple choice is shuffled
        List<String> options = new ArrayList<>();
        if ("boolean".equals(q.getType())) {
            options.add("True");
            options.add("False");
        } else {
            options.add(q.getCorrect_answer());
            if (q.getIncorrect_answers() != null) {
                options.addAll(q.getIncorrect_answers());
            }
            Collections.shuffle(options);
        }
        Log.d("QUIZ_DEBUG", "type=" + q.getType() + " options=" + options
                + " incorrect=" + q.getIncorrect_answers());

        for (int i = 0; i < answerButtons.length; i++) {
            Button b = answerButtons[i];
            b.setBackgroundTintList(defaultTint);
            if (i < options.size()) {
                b.setVisibility(View.VISIBLE);
                b.setText(options.get(i));
            } else {
                b.setVisibility(View.GONE);
            }
        }
    }

    // Feedback: green for correct, red for wrong plus the right answer shown
    private void checkAnswer(Button chosen) {
        if (answered) return;
        answered = true;

        String correct = questions.get(index).getCorrect_answer();
        String chosenText = chosen.getText().toString();

        if (chosenText.equals(correct)) {
            score++;
            chosen.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            tvFeedback.setTextColor(GREEN);
            tvFeedback.setText("Correct!");
        } else {
            chosen.setBackgroundTintList(ColorStateList.valueOf(RED));
            for (Button b : answerButtons) {
                if (b.getVisibility() == View.VISIBLE && b.getText().toString().equals(correct)) {
                    b.setBackgroundTintList(ColorStateList.valueOf(GREEN));
                }
            }
            tvFeedback.setTextColor(RED);
            tvFeedback.setText("Incorrect. The correct answer is: " + correct);
        }
        btnNext.setVisibility(View.VISIBLE);
    }

    // Shows the score and saves it straight away, so the quiz can't be played twice
    private void finishQuiz() {
        layoutQuestion.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
        tvScore.setText("Your score: " + score + " / " + questions.size());

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("completedAt", System.currentTimeMillis());
        resultRef.setValue(result)
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Could not save your score: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Saves the 1 to 5 rating next to the score
    private void submitRating() {
        int rating = (int) rbPlayerRating.getRating();
        if (rating < 1) {
            Toast.makeText(this, "Choose a rating from 1 to 5", Toast.LENGTH_SHORT).show();
            return;
        }
        resultRef.child("rating").setValue(rating)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Could not save your rating: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}