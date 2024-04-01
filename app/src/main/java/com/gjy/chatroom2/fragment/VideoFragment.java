package com.gjy.chatroom2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gjy.chatroom2.R;
import com.gjy.chatroom2.activities.VideoChatActivity;

public class VideoFragment extends Fragment {
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_video, container, false);

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