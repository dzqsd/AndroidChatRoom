package com.gjy.chatroom2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import com.gjy.chatroom2.Mysqliteopenhelper;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.fragment.PersonalCenterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UpdatePasswordActivity extends AppCompatActivity {
    private EditText et_old_password;
    private EditText et_new_password;
    private EditText et_confirm_new_password;
    private PersonalCenterFragment personalCenterFragment;

    private Mysqliteopenhelper mysqliteopenhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        //创建数据库对象
        mysqliteopenhelper = new Mysqliteopenhelper(this);

        //初始化控件
        et_old_password = findViewById(R.id.et_old_password);
        et_new_password = findViewById(R.id.et_new_password);
        et_confirm_new_password = findViewById(R.id.et_confirm_new_password);

        //修改密码按钮点击事件
        findViewById(R.id.update_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(UpdatePasswordActivity.this, "我被点了", Toast.LENGTH_SHORT).show();
                //获取输入的旧密码、新密码、确认新密码
                String oldPassword = et_old_password.getText().toString();
                String newPassword = et_new_password.getText().toString();
                String confirmNewPassword = et_confirm_new_password.getText().toString();

                //判断输入的密码是否为空
                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    //密码不能为空
                    Toast.makeText(UpdatePasswordActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmNewPassword)) {
                    //两次输入的新密码不一致
                    Toast.makeText(UpdatePasswordActivity.this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    //修改密码
                    //SigninActivity中将登录成功的用户信息保存到SharedPreferences中，这里直接读取SharedPreferences
                    //SharedPreferences.Editor editor = getSharedPreferences("user_info", MODE_PRIVATE).edit();
                    //获取正确的用户名和密码
                    SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
                    String username = sharedPreferences.getString("username", "fail");
                    String password = sharedPreferences.getString("password", "fail");
//                    Toast.makeText(UpdatePasswordActivity.this,
//                            "用户名：" + username + " 密码：" + password, Toast.LENGTH_SHORT).show();
                    if (!username.equals("fail") && oldPassword.equals(password)) {
                        //从数据库中修改密码
                        int row = mysqliteopenhelper.updatePassword(username, newPassword);
                        if (row > 0) {
                            //修改成功
                            Toast.makeText(UpdatePasswordActivity.this, "修改成功,请重新登录", Toast.LENGTH_SHORT).show();
                            //在PersonalCenterFragment类的onActivityResult()方法中要用
                            //回传时要用startActivityForResult()方法启动一个页面，并在该页面要设置setResult()方法
                            //然后在onActivityResult()方法中获取返回的数据，并做出相应的处理。
                            setResult(1000);
                            finish();
                        } else {
                            //修改失败
                            Toast.makeText(UpdatePasswordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //修改失败
                        Toast.makeText(UpdatePasswordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        //点击toolbar返回按钮，回到个人中心页面
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //结束当前Activity，返回到上一个Activity
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                //传递参数，显示个人中心页面
                intent.putExtra("showPersonalCenter", true);
                startActivity(intent);

                finish();

                //startActivity(new Intent(getApplicationContext(), UserActivity.class));

            }
        });

    }


}
