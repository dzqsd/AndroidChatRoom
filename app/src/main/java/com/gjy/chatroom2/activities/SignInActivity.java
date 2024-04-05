package com.gjy.chatroom2.activities;

import android.content.SharedPreferences;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gjy.chatroom2.Mysqliteopenhelper;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    //登录界面
    private Button login;
    private EditText inputName, inputPassword;
    private CheckBox checkBox;
    private TextView goWelcome;
    private boolean is_login;
    private SharedPreferences mSharedPreferences;

    private Mysqliteopenhelper mysqliteopenhelper;

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mysqliteopenhelper = new Mysqliteopenhelper(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_sign_in);
        setContentView(binding.getRoot());
        setListeners();
        find();

        //获取SharedPreferences实例
        mSharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);

        //是否勾选了记住密码
        is_login = mSharedPreferences.getBoolean("is_login", false);
        if(is_login){
            String username = mSharedPreferences.getString("username", null);
            String password = mSharedPreferences.getString("password", null);
            inputName.setText(username);
            inputPassword.setText(password);
            checkBox.setChecked(true);
        }

        //checkbox的点击事件
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is_login = isChecked;
            }
        });


        //goWelcome点击事件
        goWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
            }
        });
    }


    private void find() {
        login = findViewById(R.id.buttonSignIn);

        inputName = findViewById(R.id.inputName);
        inputPassword = findViewById(R.id.inputPassword);
        checkBox = findViewById(R.id.checkbox);
        goWelcome = findViewById(R.id.goWelcome);

        login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        //Toast.makeText(this, "登录被点了!", Toast.LENGTH_SHORT).show();
        //Log.d("myTest", "login success!");
        String s1 = inputName.getText().toString();
        String s2 = inputPassword.getText().toString();
        //User user = new User(s1, s2);
        boolean login = mysqliteopenhelper.login(s1, s2);

        //判断输入是否为空
        if (s1.isEmpty() || s2.isEmpty()) {
            Toast.makeText(this, "用户名或密码不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        //判断用户名是否存在
        if (!mysqliteopenhelper.isUserExist(s1)) {
            Toast.makeText(this, "用户名不存在!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (login) {
            //登录成功
            Toast.makeText(this, "登录成功!", Toast.LENGTH_SHORT).show();

            // 保存用户名和密码到 SharedPreferences 中
            SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
            editor.putString("username", s1);
            editor.putString("password", s2);
            //状态记录为登录状态
            editor.putBoolean("is_login", is_login);
            editor.apply();

            // 登录成功后跳转到用户界面
            Intent i = new Intent(this, UserActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "登录失败!", Toast.LENGTH_SHORT).show();
        }
    }


    private void setListeners() {
        //注册新用户
        binding.textCreateNewAccount.setOnClickListener(v -> {
            //Toast.makeText(this, "被点了", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        });
    }

}