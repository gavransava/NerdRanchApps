package com.example.savagavran.geoquiz;

/**
 * Created by sava.gavran on 10/4/2016.
 */

public class Question {

    private int mTextResId;
    private boolean mAnswerTrue;

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }

    public Question(int TextResId, boolean AnswerTrue) {
        mTextResId = TextResId;
        mAnswerTrue = AnswerTrue;
    }
}
