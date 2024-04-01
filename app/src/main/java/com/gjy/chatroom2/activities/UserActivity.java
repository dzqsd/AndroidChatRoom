package com.gjy.chatroom2.activities;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.databinding.ActivityUserBinding;
import com.gjy.chatroom2.fragment.VideoFragment;
import com.gjy.chatroom2.fragment.HomeFragment;
import com.gjy.chatroom2.fragment.PersonalCenterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;
    private VideoFragment videoFragment;
    private PersonalCenterFragment personalCenterFragment;
    private ActivityUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        find();
        setListeners();

        //初始化控件
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        //默认首页被选中
        selectFragment(0);

        // 接收来自 UpdatePasswordActivity 或QRCodeActivity 的 Intent
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("showPersonalCenter", false)) {
            // 如果传递过来的标志为 true，则显示个人中心页面
            bottomNavigationView.setSelectedItemId(R.id.personalCenter);
            selectFragment(2);
        } else if (intent != null && intent.getBooleanExtra("showHome", false)) {
            bottomNavigationView.setSelectedItemId(R.id.home);
            selectFragment(0);
        }

        //bottomNavigationView的点击事件
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.home){
                    selectFragment(0);
                }else if(item.getItemId()==R.id.addressList){
                    selectFragment(1);
                }else if(item.getItemId()==R.id.personalCenter){
                    selectFragment(2);
                }

                return true;
            }
        });
    }

    //选中Fragment
    private void selectFragment(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        hideFragment(fragmentTransaction);

        if (position == 0) {
            if (homeFragment == null) {
                homeFragment = new HomeFragment();
                fragmentTransaction.add(R.id.content, homeFragment);
            } else {
                fragmentTransaction.show(homeFragment);
            }
        } else if (position == 1) {
            if (videoFragment == null) {
                videoFragment = new VideoFragment();
                fragmentTransaction.add(R.id.content, videoFragment);
            } else {
                fragmentTransaction.show(videoFragment);
            }
        } else if (position == 2) {
            if (personalCenterFragment == null) {
                personalCenterFragment = new PersonalCenterFragment();
                fragmentTransaction.add(R.id.content, personalCenterFragment);
            } else {
                fragmentTransaction.show(personalCenterFragment);
            }
        }
        //记住要提交事务
        fragmentTransaction.commit();
    }

    //选中一个Fragment后，其它Fragment要隐藏
    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (homeFragment != null) {
            fragmentTransaction.hide(homeFragment);
        }
        if (videoFragment != null) {
            fragmentTransaction.hide(videoFragment);
        }
        if (personalCenterFragment != null) {
            fragmentTransaction.hide(personalCenterFragment);
        }


    }

    private void setListeners() {
        binding.backToSignIn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignInActivity.class)));
    }

    private void find() {

    }
}