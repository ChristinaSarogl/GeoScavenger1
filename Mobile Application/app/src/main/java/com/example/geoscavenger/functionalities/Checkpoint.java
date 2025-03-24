package com.example.geoscavenger.functionalities;

import com.google.firebase.firestore.GeoPoint;


import java.util.ArrayList;
import java.util.List;

public class Checkpoint {
    private String mCheckpointID;
    private String mQuestion;
    private String mClue;
    private GeoPoint mLocation;
    private List<String> mAnswers = new ArrayList<>();
    private Long mRightAnswerIndex;

    public Checkpoint(){}

    public String getCheckpointID() {
        return mCheckpointID;
    }

    public void setCheckpointID(String checkpointID) {
        mCheckpointID = checkpointID;
    }

    public String getQuestion() {
        return mQuestion;
    }

    public void setQuestion(String question) {
        mQuestion = question;
    }

    public String getClue() {
        return mClue;
    }

    public void setClue(String clue) {
        mClue = clue;
    }

    public GeoPoint getLocation() {
        return mLocation;
    }

    public void setLocation(GeoPoint location) {
        mLocation = location;
    }

    public List<String> getAnswers() {
        return mAnswers;
    }

    public void setAnswers(List<String> answers) {
        mAnswers = answers;
    }

    public Long getRightAnswerIndex() {
        return mRightAnswerIndex;
    }

    public void setRightAnswerIndex(Long rightAnswerIndex) {
        mRightAnswerIndex = rightAnswerIndex;
    }
}
