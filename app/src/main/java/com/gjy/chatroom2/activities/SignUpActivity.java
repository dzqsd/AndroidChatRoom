package com.gjy.chatroom2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gjy.chatroom2.Mysqliteopenhelper;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.databinding.ActivitySignUpBinding;
import com.gjy.chatroom2.javabean.User;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    //注册页面
    private ActivitySignUpBinding binding;

    private Button register;
    private EditText name, password, confirmPassword;

    private Mysqliteopenhelper mysqliteopenhelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mysqliteopenhelper = new Mysqliteopenhelper(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        find();
    }

    private void find() {
        register = findViewById(R.id.buttonSignUp);

        name = findViewById(R.id.inputName);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);

        register.setOnClickListener(this);

    }


    private void setListeners() {
        //点击登录按钮返回上一页面（登录界面）
        binding.backToSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, SignInActivity.class)));
    }


    //点击注册按钮
    public void signUp(View view) {
        //Toast.makeText(this, "注册被点了!", Toast.LENGTH_SHORT).show();
        String s1 = name.getText().toString();
        String s2 = password.getText().toString();
        String s3 = confirmPassword.getText().toString();

        //判断输入是否为空
        if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty()) {
            Toast.makeText(this, "输入不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断两次密码是否一致
        if (!s2.equals(s3)) {
            Toast.makeText(this, "两次密码不一致!", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断用户名是否已存在
        if (mysqliteopenhelper.isUserExist(s1)) {
            Toast.makeText(this, "用户名已存在!", Toast.LENGTH_SHORT).show();
            return;
        }

        User u = new User(s1, s2);
        long l = mysqliteopenhelper.register(u);
        if (l != -1) {
            //注册成功
            Toast.makeText(this, "注册成功!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "注册失败!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        signUp(v);
    }
}