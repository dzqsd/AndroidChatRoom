package com.gjy.chatroom2.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.gjy.chatroom2.R;

import java.io.*;
import java.net.Socket;

public class HomeFragment extends Fragment {


    private EditText input_text;//要发送的内容
    private RecyclerView mRecyclerView;

    private EditText messageTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        input_text = view.findViewById(R.id.input_text);
        //mRecyclerView = view.findViewById(R.id.message_view);
        //messageTextView = view.findViewById(R.id.message_view);


        //发送按钮的点击事件
        view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        return view;
    }

}
