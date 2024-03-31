package com.gjy.chatroom2.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.activities.AboutUsActivity;
import com.gjy.chatroom2.activities.ChatRoomActivity;
import com.gjy.chatroom2.activities.MainActivity;
import com.gjy.chatroom2.activities.UserActivity;

import java.io.*;
import java.net.Socket;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //View view = inflater.inflate(R.layout.fragment_home, container, false);
        rootView = inflater.inflate(R.layout.fragment_home, container, false);


        //点击开始聊天按钮
        rootView.findViewById(R.id.btn_startChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转至聊天页面
                Toast.makeText(getContext(), "开始聊天！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), ChatRoomActivity.class));
            }
        });


        return rootView;
    }

    @Override
    public void onClick(View v) {

    }
}
