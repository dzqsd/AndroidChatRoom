package com.gjy.chatroom2.activities;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gjy.chatroom2.Mysqliteopenhelper;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.databinding.ActivitySignInBinding;
import com.gjy.chatroom2.databinding.ActivitySignUpBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    //登录界面
    private Button login;
    private EditText name, password;

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
    }

    private void find() {
        login = findViewById(R.id.buttonSignIn);

        name = findViewById(R.id.inputName);
        password = findViewById(R.id.inputPassword);

        login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        //Toast.makeText(this, "登录被点了!", Toast.LENGTH_SHORT).show();
        String s1 = name.getText().toString();
        String s2 = password.getText().toString();
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
            editor.apply();

            // 登录成功后跳转到用户界面
            Intent i = new Intent(this, UserActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "登录失败!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v -> {
            //Toast.makeText(this, "被点了", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        });
    }

}