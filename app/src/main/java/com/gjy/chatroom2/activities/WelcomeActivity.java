package com.gjy.chatroom2.activities;

import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.gjy.chatroom2.R;

public class WelcomeActivity extends AppCompatActivity {
    private TextView tv_countdown;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 6000;//倒计时时长 5000毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //初始化控件
        tv_countdown = findViewById(R.id.tv_countdown);

        //启动倒计时
        startCountDown();

        //点击倒计时可以提前结束倒计时
        tv_countdown.setOnClickListener(v -> {
            countDownTimer.cancel();
            startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
            finish();
        });
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int secondsRemaining = (int) (timeLeftInMillis / 1000);
                tv_countdown.setText(secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                //倒计时结束，跳转到登录页面
                startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}