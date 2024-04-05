package com.gjy.chatroom2.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.activities.ChatRoomActivity;
import com.gjy.chatroom2.activities.UserActivity;
import com.gjy.chatroom2.activities.VideoChatActivity;

public class VideoFragment extends Fragment {
    private View rootView;
    private TextView tv_inputToken;
    private String Token;
    private SharedPreferences mSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_video, container, false);

        // 输入Token的点击事件
        tv_inputToken = rootView.findViewById(R.id.tv_inputToken);
        tv_inputToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 加载布局
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View layout = inflater.inflate(R.layout.input_token, null);
                // 通过对 AlertDialog.Builder 对象调用 setView()
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(R.layout.start_server);
                builder.setCancelable(false);//是否为可取消
                // 加载控件
                EditText editToken = (EditText) layout.findViewById(R.id.editToken);

                new AlertDialog.Builder(getContext())
                        .setView(layout)  // 设置显示内容
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Token = editToken.getText().toString();
                                Toast.makeText(getContext(), "Token：" + Token, Toast.LENGTH_SHORT).show();
                                //将Token存入SharedPreferences
                                mSharedPreferences = getActivity().getSharedPreferences("Token", 0);
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString("Token", Token);
                                editor.apply();

                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)  // 按回退键不可取消该对话框
                        .show();
            }
        });

        //点击视频聊天按钮
        rootView.findViewById(R.id.btn_startVideoChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转至聊天页面
                Toast.makeText(getContext(), "开始视频聊天！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), VideoChatActivity.class));
            }
        });


        return rootView;

    }
}