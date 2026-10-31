package cs.kirk.assignment2_task2.Quiz;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CategoryController implements Callback<CategoryResponse> {

    final String BASE_URL = "https://opentdb.com/";
    private final CategoryListener listener;

    public interface CategoryListener {
        void onCategoriesLoaded(List<Category> categoryList);
        void onCategoriesError(String message);
    }

    public CategoryController(CategoryListener listener){
        this.listener = listener;
    }

    public void start() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        QuizRESTAPI quizRESTAPI = retrofit.create(QuizRESTAPI.class);
        Call<CategoryResponse> call = quizRESTAPI.getCategories();
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
        if(response.isSuccessful() && response.body() != null
        && response.body().getTrivia_categories() != null
        && !response.body().getTrivia_categories().isEmpty()) {
            Log.d("QUIZ_API", "Getting Categories: " + response.body().getTrivia_categories().size());
            listener.onCategoriesLoaded(response.body().getTrivia_categories());
        } else {
            Log.d("QUIZ_API", "Category Error: " + response.code());
            listener.onCategoriesError("Could not Load Categories " + response.code());
        }
    }

    @Override
    public void onFailure(Call<CategoryResponse> call, Throwable t) {
        Log.d("QUIZ_API", "Error " + t.getMessage());
        listener.onCategoriesError("Failed to Load Categories " + t.getMessage());
    }
}
