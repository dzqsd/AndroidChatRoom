package com.gjy.chatroom2.activities;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.gjy.chatroom2.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        //点击toolbar返回按钮，回到个人中心页面
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //结束当前Activity，返回到上一个Activity
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                //传递参数，显示个人中心页面
                //UserActivity中会处理传递的参数
                intent.putExtra("showPersonalCenter", true);
                startActivity(intent);

                finish();

            }
        });

    }
}