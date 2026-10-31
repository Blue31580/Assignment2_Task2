package cs.kirk.assignment2_task2.Quiz;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Question {
    @SerializedName("type")
    private String type;
    @SerializedName("difficulty")

    private String difficulty;
    @SerializedName("category")
    private String category;
    @SerializedName("question")
    private String question;
    @SerializedName("correct_answer")
    private String correct_answer;
    @SerializedName("incorrect_answer")
    private List<String> incorrect_answer;

    public Question(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrect_answer() {
        return correct_answer;
    }

    public void setCorrect_answer(String correct_answer) {
        this.correct_answer = correct_answer;
    }

    public List<String> getIncorrect_answer() {
        return incorrect_answer;
    }

    public void setIncorrect_answer(List<String> incorrect_answer) {
        this.incorrect_answer = incorrect_answer;
    }
}
