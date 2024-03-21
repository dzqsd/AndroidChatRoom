package com.gjy.chatroom2.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gjy.chatroom2.Mysqliteopenhelper;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.activities.AboutUsActivity;
import com.gjy.chatroom2.activities.QRcodeActivity;
import com.gjy.chatroom2.activities.SignInActivity;
import com.gjy.chatroom2.activities.UpdatePasswordActivity;
import com.gjy.chatroom2.databinding.ActivitySignUpBinding;
import com.gjy.chatroom2.databinding.FragmentPersonalCenterBinding;
import com.gjy.chatroom2.javabean.User;

import static android.content.Context.MODE_PRIVATE;

public class PersonalCenterFragment extends Fragment {
    private View rootView;
    private TextView tv_username;
    private TextView tv_signature;
    private FragmentPersonalCenterBinding binding;
    private Mysqliteopenhelper mysqliteopenhelper;

    private ImageButton logout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 在视图创建后设置点击事件
        setListeners();
    }

    private void setListeners() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPersonalCenterBinding.inflate(inflater, container, false);
        rootView = inflater.inflate(R.layout.fragment_personal_center, container, false);

        //初始化控件
        tv_username = rootView.findViewById(R.id.tv_username);
        tv_signature = rootView.findViewById(R.id.tv_signature);


        //点击退出登录按钮
        rootView.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("提示")
                        .setMessage("您确定要退出登录吗?")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //不退出登录
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //退出登录
                                getActivity().finish();

                                //跳转到登录界面
                                startActivity(new Intent(getActivity(), SignInActivity.class));
                            }
                        })
                        .show();
            }
        });


        //前往修改密码页面
        rootView.findViewById(R.id.update_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UpdatePasswordActivity.class);
                startActivityForResult(intent, 1000);
            }
        });

        //点击注销用户
        rootView.findViewById(R.id.delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取当前登录的用户名
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_info", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("username", "");

                //Toast.makeText(getContext(), "用户："+savedUsername, Toast.LENGTH_SHORT).show();
                //弹出警告框
                new AlertDialog.Builder(getContext())
                        .setTitle("警告!警告！")
                        .setMessage("您确定要注销用户" + savedUsername + "吗?\n注销后账户信息将永久消失!")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //不注销用户
                            }
                        })
                        .setPositiveButton("我确定!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //注销用户
                                mysqliteopenhelper = new Mysqliteopenhelper(getContext());
                                mysqliteopenhelper.deleteUser(savedUsername);
                                Toast.makeText(getContext(), "注销成功,再见" + savedUsername + "!", Toast.LENGTH_SHORT).show();

                                //退出登录
                                getActivity().finish();

                                //跳转到登录界面
                                startActivity(new Intent(getActivity(), SignInActivity.class));
                            }
                        })
                        .show();
            }
        });

        //前往联系客服页面
        rootView.findViewById(R.id.contact_customer_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), QRcodeActivity.class));
            }
        });

        //前往关于我们界面
        rootView.findViewById(R.id.about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutUsActivity.class));
            }
        });


        return rootView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //获取用户名
        SharedPreferences prefs = getActivity().getSharedPreferences("user_info", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "");
        //String savedpassword = prefs.getString("password", "");
        tv_username.setText(savedUsername);
        tv_signature.setText("大家好,我是帅气的" + savedUsername + "!");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            //修改成功后，返回登录界面
            getActivity().finish();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        }
    }
}