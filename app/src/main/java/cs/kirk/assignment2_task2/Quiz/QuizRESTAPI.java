package cs.kirk.assignment2_task2.Quiz;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuizRESTAPI {

    @GET("api.php")
    Call<QuizResponse> getQuestions(
            @Query("amount") int amount,
            @Query("category") Integer category,
            @Query("difficulty") String difficulty,
            @Query("type") String type,
            @Query("encode") String encode);

    @GET("api_category.php")
    Call<CategoryResponse> getCategories();
}
