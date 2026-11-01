package cs.kirk.assignment2_task2.Quiz;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizController implements Callback<QuizResponse> {

    public interface QuizListener {
        void onQuestionsLoaded(List<Question> questions);
        void onError(String message);
    }

    final String BASE_URL = "https://opentdb.com";
    private final QuizListener listener;

    public QuizController(QuizListener listener) {
        this.listener = listener;
    }

    public void start(Integer category, String difficulty, String type) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        QuizRESTAPI quizRESTAPI = retrofit.create(QuizRESTAPI.class);
        Call<QuizResponse> call = quizRESTAPI.getQuestions(10, category, difficulty, type,"url3986");
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
        if(response.code() == 429) {
            Log.d("QUIZ_API", "");
            listener.onError("Too many requests");
            return;
        }
        if(!response.isSuccessful() || response.body() == null) {
            Log.d("QUIZ_API", "Server Error: " + response.code());
            listener.onError("Server Error: " + response.code());
            return;
        }

        QuizResponse body = response.body();
        if(body.getResponse_code() != 0 || body.getResults() == null || body.getResults().isEmpty()) {
            Log.d("QUIZ_API", "" + body.getResponse_code());
            listener.onError("Noot Enough Questions");
            return;
        }

        List<Question> questions = body.getResults();
        for(Question q : questions) {
            decode(q);
        }
        Log.d("QUIZ_API", "Getting questions successfully: " + questions.size());
        listener.onQuestionsLoaded(questions);
    }

    public void onFailure(Call<QuizResponse> call, Throwable t) {
        Log.d("QUIZ_API", "Error getting questions: " + t.getMessage());
        listener.onError("Fail" + t.getMessage());
    }

    private void decode(Question q) {
        q.setType(Uri.decode(q.getType()));
        q.setDifficulty(Uri.decode(q.getDifficulty()));
        q.setCategory(Uri.decode(q.getDifficulty()));
        q.setQuestion(Uri.decode(q.getQuestion()));
        q.setCorrect_answer(Uri.decode(q.getCorrect_answer()));

        List<String> decoded = new ArrayList<>();
        if(q.getIncorrect_answers() != null) {
            for(String wrong : q.getIncorrect_answers()) {
                decoded.add(Uri.decode(wrong));
            }
        }
        q.setIncorrect_answers(decoded);
    }

}
