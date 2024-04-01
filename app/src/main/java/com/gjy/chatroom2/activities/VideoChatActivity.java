package com.gjy.chatroom2.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.gjy.chatroom2.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import android.os.Bundle;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.ChannelMediaOptions;

public class VideoChatActivity extends AppCompatActivity {

    // 填写项目的 App ID，可在声网控制台中生成
    private String appId = "dc06c3be8bc74c8d830fbff83eb4635d";
    // 填写频道名
    private String channelName = "gjy";
    // 填写声网控制台中生成的临时 Token
    private String token =
            "007eJxTYJhcunpGhy5XpHJl2dlZgUrfLtuVHwlJ7l6w0TrIymrxRQUFhpRkA7Nk46RUi6Rkc5NkixQLY4O0pLQ0C+PUJBMzY9OUM0VcaQ2BjAyiKXksjAwQCOIzM6RnVTIwAAD4jR4Q";

    private RtcEngine mRtcEngine;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 监听频道内的远端用户，获取用户的 uid 信息
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 获取 uid 后，设置远端视频视图
                    setupRemoteVideo(uid);
                }
            });
        }
    };

    private void initializeAndJoinChannel() {
        try {
            // 创建 RtcEngineConfig 对象，并进行配置
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            // 创建并初始化 RtcEngine
            mRtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
        // 启用视频模块
        mRtcEngine.enableVideo();
        // 开启本地预览
        mRtcEngine.startPreview();

        // 创建一个 SurfaceView 对象，并将其作为 FrameLayout 的子对象
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        container.addView(surfaceView);
        // 将 SurfaceView 对象传入声网实时互动 SDK，设置本地视图
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));

        // 创建 ChannelMediaOptions 对象，并进行配置
        ChannelMediaOptions options = new ChannelMediaOptions();
        // 根据场景将用户角色设置为 BROADCASTER (主播) 或 AUDIENCE (观众)
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        // 直播场景下，设置频道场景为 BROADCASTING (直播场景)
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

        // 使用临时 Token 加入频道，自行指定用户 ID 并确保其在频道内的唯一性
        mRtcEngine.joinChannel(token, channelName, 0, options);
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        // 将 SurfaceView 对象传入声网实时互动 SDK，设置远端视图
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
    }

    private static final int PERMISSION_REQ_ID = 22;

    // 获取体验实时音视频互动所需的录音、摄像头等权限
    private String[] getRequiredPermissions() {
        // 判断 targetSDKVersion 31 及以上时所需的权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO, // 录音权限
                    Manifest.permission.CAMERA, // 摄像头权限
                    Manifest.permission.READ_PHONE_STATE, // 读取电话状态权限
                    Manifest.permission.BLUETOOTH_CONNECT // 蓝牙连接权限
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        // 如果已经授权，则初始化 RtcEngine 并加入频道
        if (checkPermissions()) {
            initializeAndJoinChannel();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 停止本地视频预览
        mRtcEngine.stopPreview();

        // 离开频道
        mRtcEngine.leaveChannel();
    }
}
