package com.gjy.chatroom2.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.javabean.BannerDataInfo;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.listener.OnBannerListener;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private TextView tv_countdown;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 30000;//倒计时时长 30000毫秒 30秒
    private Banner banner;
    private List<BannerDataInfo> mBannerDataInfos = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //初始化控件
        tv_countdown = findViewById(R.id.tv_countdown);
        banner = findViewById(R.id.banner);

        //图片轮播功能
        mBannerDataInfos.add(new BannerDataInfo(R.drawable.dog1, "我是cc1"));
        mBannerDataInfos.add(new BannerDataInfo(R.drawable.dog2, "我是cc2"));
        mBannerDataInfos.add(new BannerDataInfo(R.drawable.wechat2, "我是cc3"));

        //设置轮播间隔时间
        banner.setLoopTime(3000);//3000毫秒切换一次图片
        //设置adapter
        banner.setAdapter(new BannerImageAdapter<BannerDataInfo>(mBannerDataInfos) {
                    @Override
                    public void onBindView(BannerImageHolder bannerImageHolder, BannerDataInfo bannerDataInfo, int i, int i1) {
                        //设置图片
                        bannerImageHolder.imageView.setImageResource(bannerDataInfo.getImg());
                    }

                })
                .addBannerLifecycleObserver(this)
                .setIndicator(new CircleIndicator(this));

        //画廊效果
        //banner.setBannerGalleryEffect(10,10);

        //魅族效果
        banner.setBannerGalleryMZ(10);
        //banner设置点击事件
        banner.setOnBannerListener(new OnBannerListener<BannerDataInfo>() {
            @Override
            public void OnBannerClick(BannerDataInfo bannerDataInfo, int i) {
                Toast.makeText(WelcomeActivity.this, bannerDataInfo.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

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