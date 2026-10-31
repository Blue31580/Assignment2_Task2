package cs.kirk.assignment2_task2.Quiz;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryResponse {
    @SerializedName("trivia_categories")
    private List<Category> trivia_categories;

    public List<Category> getTrivia_categories() {
        return trivia_categories;
    }

    public void setTrivia_categories(List<Category> trivia_categories) {
        this.trivia_categories = trivia_categories;
    }
}
