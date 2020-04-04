package com.example.quizactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;

    private int mCurrentScore;

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWERED_ARRAY = "answered_array";
    private static final String KEY_SCORE = "score";
    private static final String KEY_CHEATED_ARRAY = "cheated_array";

    private static final int REQUEST_CODE_CHEAT = 0;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private boolean[] mQuestionAnswered = new boolean[]{
            false,
            false,
            false,
            false,
            false,
            false,
    };

    private boolean[] mCheated = new boolean[] {
            false,
            false,
            false,
            false,
            false,
            false,
    };

    private int mCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        // restore the instance state
        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mQuestionAnswered = savedInstanceState.getBooleanArray(KEY_ANSWERED_ARRAY);
            mCurrentScore = savedInstanceState.getInt(KEY_SCORE, 0);
            mCheated = savedInstanceState.getBooleanArray(KEY_CHEATED_ARRAY);
        }

        // set up text view
        mQuestionTextView = findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
                updateAnswerButtons();
            }
        });

        // set up true button
        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
                updateAnswerButtons();
            }
        });

        // set up false button
        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new FalseButtonListener());

        // set up next button
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
                updateAnswerButtons();
            }
        });

        // set up prev button
        mPrevButton = findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex - 1 + mQuestionBank.length) % mQuestionBank.length;
                updateQuestion();
                updateAnswerButtons();
            }
        });

        // set up cheat button
        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        // update the question
        updateQuestion();  // update question after setting up answer buttons as update questions
        // calls updateAnswerButtons()

        // update answer buttons
        updateAnswerButtons();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mCheated[mCurrentIndex] = CheatActivity.wasAnswerShown(data);
        }
    }

    class FalseButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            checkAnswer(false);
            updateAnswerButtons();
        }
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        mQuestionAnswered[mCurrentIndex] = true;
        int messageResId;

        if (mCheated[mCurrentIndex]) {
            messageResId = R.string.judgement_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mCurrentScore++;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        checkQuizComplete();
    }

    public void checkQuizComplete() {
        boolean allAnswered = true;
        int messageCompleteId;
        for (boolean answered : mQuestionAnswered) {
            if (!answered) {
                allAnswered = false;
                break;
            }
        }
        if (allAnswered) {
            messageCompleteId = R.string.quiz_complete_message;
            double scoreInPercent = ((double) mCurrentScore / mQuestionBank.length) * 100;
            String message = String.format(Locale.ENGLISH, "%s %.2f", getString(messageCompleteId), scoreInPercent);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateAnswerButtons() {
        if (mQuestionAnswered[mCurrentIndex]) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        } else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBooleanArray(KEY_ANSWERED_ARRAY, mQuestionAnswered);
        savedInstanceState.putInt(KEY_SCORE, mCurrentScore);
        savedInstanceState.putBooleanArray(KEY_CHEATED_ARRAY, mCheated);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
