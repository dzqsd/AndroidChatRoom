package com.gjy.chatroom2.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.gjy.chatroom2.R;

import androidx.appcompat.app.AppCompatActivity;

import com.gjy.chatroom2.javabean.MessageInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    private TextView titleView;
    private EditText sendMessageText;
    private int startPort;
    private boolean isConnected = true, isServer = false;
    private String message = "", userSendMsg = "", titleText = "";
    private String[] connectServerData = new String[2];// 0.ipv4 1.端口号
    private Long mID = 0L;
    private List<MessageInfo> datas = new ArrayList<MessageInfo>();
    private SimpleDateFormat simpleDateFormat;
    private MessageAdapter messageAdapter;
    private static Socket socket = null;//用于与服务端通信的Socket
    private static ServerSocket server;
    private static List<PrintWriter> allOut; //存放所有客户端的输出流的集合，用于广播

    private static final int IMAGE = 1;//调用系统相册-选择图片
    private static String[] PERMISSIONS_STORAGE = {
            //依次权限申请
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        //删除这个就不闪退了
        //getSupportActionBar().hide();//隐藏标题栏
        applyPermission();
        InitView();
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    titleView.setText(titleText);
                } else if (msg.what == 2) {
                    titleView.setText("当前在线人数[" + (allOut.size() + 1) + "]");
                }
                super.handleMessage(msg);
            }
        };

        //设置点击事件

        //点击toolbar返回按钮，回到首页
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //结束当前Activity，返回到上一个Activity
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                //传递参数，显示首页
                //UserActivity中会处理传递的参数
                intent.putExtra("showHome", true);
                startActivity(intent);

                finish();

            }
        });

        // 开启服务器按钮点击事件
        Button btn_startServer = (Button) findViewById(R.id.btn_startServer);
        btn_startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 加载布局
                LayoutInflater inflater = LayoutInflater.from(ChatRoomActivity.this);
                View layout = inflater.inflate(R.layout.start_server, null);
                // 通过对 AlertDialog.Builder 对象调用 setView()
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomActivity.this);
                builder.setView(R.layout.start_server);
                builder.setCancelable(false);//是否为可取消
                // 加载控件
                EditText editPort = (EditText) layout.findViewById(R.id.editPort);

                new AlertDialog.Builder(ChatRoomActivity.this)
                        .setView(layout)  // 设置显示内容
                        .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startPort = Integer.valueOf(editPort.getText().toString());
                                mID = System.currentTimeMillis();
                                ServerInit();
                                Toast.makeText(ChatRoomActivity.this, "服务器已启动，端口号：" + startPort, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)  // 按回退键不可取消该对话框
                        .show();
            }
        });

        // 连接服务器按钮点击事件
        Button btn_connectServer = (Button) findViewById(R.id.btn_connectServer);
        btn_connectServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 加载布局
                LayoutInflater inflater = LayoutInflater.from(ChatRoomActivity.this);
                View layout = inflater.inflate(R.layout.connect_server, null);
                // 通过对 AlertDialog.Builder 对象调用 setView()
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomActivity.this);
                builder.setView(R.layout.connect_server);
                builder.setCancelable(false);//是否为可取消
                // 加载控件
                EditText editIpv4Text = (EditText) layout.findViewById(R.id.editIpv4Text);
                EditText editPortText = (EditText) layout.findViewById(R.id.editPortText);

                new AlertDialog.Builder(ChatRoomActivity.this)
                        .setView(layout)  // 设置显示内容
                        .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                connectServerData[0] = editIpv4Text.getText().toString();
                                connectServerData[1] = editPortText.getText().toString();
                                Toast.makeText(ChatRoomActivity.this, "正在连接服务器" + connectServerData[0] + ":" + connectServerData[1]
                                        , Toast.LENGTH_SHORT).show();

                                boolean isConnected = ConnectSever();
                                if(isConnected){
                                    Toast.makeText(ChatRoomActivity.this, "连接服务器成功", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)  // 按回退键不可取消该对话框
                        .show();
            }
        });

        // 发送消息按钮点击事件
        Button btn_sendMessage = (Button) findViewById(R.id.btn_sendMessage);
        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", "unknown"); // 获取用户名

                message = sendMessageText.getText().toString();
                if (message == null || "".equals(message)) {
                    Toast.makeText(ChatRoomActivity.this, "发送消息不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                long Ltimes = System.currentTimeMillis();

                if (isServer) {
                    //服务器
                    MessageInfo m = new MessageInfo(message, Ltimes, mID, "1", username);
                    datas.add(m);

                    //sendMessage("{\"isimg\":\"1\",\"msg\":\"" + message + "\",\"times\":\"" + Ltimes + "\",\"id\":\"" + mID + "\",\"peoplen\":\"" + "当前在线人数[" + (allOut.size() + 1) + "]" + "\"}");
                    @SuppressLint("DefaultLocale")
                    String messageJson = String.format("{\"isimg\":\"1\",\"msg\":\"%s\",\"times\":\"%d\",\"id\":\"%d\", \"username\":\"%s\", \"peoplen\":\"当前在线人数[%d]\"}",
                            message, Ltimes, mID, username, (allOut.size() + 1));
                    sendMessage(messageJson);

                } else {
                    //客户端
                    sendMsgText();
                }
                //清空输入框文字
                sendMessageText.setText("");
            }
        });

        // 发送图片按钮点击事件
        ImageView btn_sendImage = (ImageView) findViewById(R.id.btn_sendImage);
        btn_sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });

    }

    //初始化控件
    private void InitView() {

        ListView showMessage = (ListView) findViewById(R.id.showmsg);
        Button startServer = (Button) findViewById(R.id.btn_startServer);
        Button continueServer = (Button) findViewById(R.id.btn_connectServer);
        Button btn_sendMessage = (Button) findViewById(R.id.btn_sendMessage);
        ImageView btn_sendImage = (ImageView) findViewById(R.id.btn_sendImage);

        titleView = (TextView) findViewById(R.id.titleview);
        sendMessageText = (EditText) findViewById(R.id.sendmsgtext);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        messageAdapter = new MessageAdapter();

        showMessage.setAdapter(messageAdapter);

        startServer.setOnClickListener(this);
        continueServer.setOnClickListener(this);
        btn_sendMessage.setOnClickListener(this);
        btn_sendImage.setOnClickListener(this);

    }

    //定义判断权限申请的函数，在onCreat中调用就行
    public void applyPermission() {
        //检查当前Android版本是否大于等于23（Android 6.0及以上）
        if (Build.VERSION.SDK_INT >= 23) {
            boolean needapply = false;
            for (int i = 0; i < PERMISSIONS_STORAGE.length; i++) {
                int checkPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        PERMISSIONS_STORAGE[i]);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    needapply = true;
                }
            }
            //需要申请权限
            if (needapply) {
                ActivityCompat.requestPermissions(ChatRoomActivity.this, PERMISSIONS_STORAGE, 1);
            }
        }
    }

    @Override
    public void onClick(View view) {
    }

    /**
     * 服务器端
     *
     * @param out
     */
    //将给定的输出流放入集合
    private synchronized void addOut(PrintWriter out) {
        allOut.add(out);
    }

    //将给定的输出流移出集合
    private synchronized void removeOut(PrintWriter out) {
        allOut.remove(out);
    }


    //将给定的消息发给客户端
    private void sendMessage(String message) {
        Thread sendMsg = new Thread(new Runnable() {
            @Override
            public void run() {
                for (PrintWriter out : allOut) {
                    out.println(message);
                }
            }
        });
        sendMsg.start();
    }

    //服务器初始化
    public void ServerInit() {
        try {
            server = new ServerSocket(startPort);
            allOut = new ArrayList<PrintWriter>();
            isServer = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            Socket socket1 = null;
                            try {
                                socket1 = server.accept();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            ClientHandler handler = new ClientHandler(socket1);
                            Thread t = new Thread(handler);
                            t.start();
                        }
                    }
                }).start();
    }


    //该线程类是与指定的客户端进行交互工作
    class ClientHandler implements Runnable {
        //当前线程客户端的Socket
        private Socket socket;

        //该客户端的地址
        private String host;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            InetAddress address = socket.getInetAddress();
            //获取ip地址
            host = address.getHostAddress();
        }

        @Override
        public void run() {
            PrintWriter pw = null;
            try {
                //有用户加入
                sendMessage("[" + host + "]加入聊天!");

                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
                pw = new PrintWriter(osw, true);

                //将该客户的输出流存入共享集合，以便消息可以广播给该客户端
                addOut(pw);

                handler.sendEmptyMessage(2);

                //处理来自客户端的数据
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, "utf-8");
                BufferedReader br = new BufferedReader(isr);

                String message = null;
                while ((message = br.readLine()) != null) {

                    try {
                        JSONObject json = new JSONObject(message);
                        String messageType = json.getString("isimg");
                        String msgContent = json.getString("msg");
                        long times = json.getLong("times");
                        long id = json.getLong("id");
                        String username = json.optString("username", "unknown"); // 使用optString以避免JSONException

                        // 根据消息类型创建MessageInfor对象，现在包括username参数
                        MessageInfo m = new MessageInfo(msgContent, times, id, messageType, username);

                        // 更新datas列表应在主线程中执行
                        final MessageInfo finalM = m;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                datas.add(finalM);
                                messageAdapter.notifyDataSetChanged(); // 通知数据源发生变化
                            }
                        });

//                        if (json.getString("isimg").equals("1")) {
//                            //不为图片
//                            datas.add(new MessageInfor(json.getString("msg"), Long.valueOf(json.getString("times")), Long.valueOf(json.getString("id")), "1"));
//                        } else if (json.getString("isimg").equals("0")) {
//                            //图片
//                            datas.add(new MessageInfor(json.getString("msg"), Long.valueOf(json.getString("times")), Long.valueOf(json.getString("id")), "0"));
//                        }

                        titleText = json.optString("peoplen", "当前在线人数[未知]"); // 使用optString以避免JSONException
                        handler.sendEmptyMessage(1);

                        //messageAdapter.notifyDataSetChanged();//通知数据源发生变化
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    sendMessage(message);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                //将该客户端的输出流从共享集合中删除
                removeOut(pw);

                //有用户退出
                sendMessage("[" + host + "]退出聊天!");

                handler.sendEmptyMessage(2);

                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 客户端
     *
     * @return
     */
    public boolean ConnectSever() {

        Thread continuethread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //localhost 127.0.0.1
                            socket = new Socket(connectServerData[0], Integer.valueOf(connectServerData[1]));
                            mID = System.currentTimeMillis();
                        } catch (Exception e) {
                            isConnected = false;
                            isServer = false;
                            e.printStackTrace();
                        }
                    }
                }
        );
        continuethread.start();

        while (isConnected) {
            if (socket != null) {
                break;
            }
        }


        if (isConnected) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            /*
                             * 客户端开始工作的方法
                             */
                            try {
                                //启动用于读取服务端发送消息的线程
                                ServerHandler handler = new ServerHandler();
                                //ServerHandler是自己写的类，实现Runnable接口,有多线程功能
                                Thread t = new Thread(handler);
                                t.start();

                                //将数据发送到服务端
                                OutputStream out = socket.getOutputStream();//获取输出流对象
                                OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");//转化成utf-8格式
                                PrintWriter pw = new PrintWriter(osw, true);
                                while (true) {
                                    if (userSendMsg != "" && userSendMsg != null) {
                                        pw.println(userSendMsg);//把信息输出到服务端
                                        userSendMsg = "";
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
        }
        return isConnected;
    }

    class ServerHandler implements Runnable {
        /**
         * 读取服务端发送过来的消息
         */
        @Override
        public void run() {
            try {
                InputStream in = socket.getInputStream();//输入流
                InputStreamReader isr = new InputStreamReader(in, "UTF-8");//以utf-8读
                BufferedReader br = new BufferedReader(isr);
                String message1 = br.readLine();

                while (message1 != null) {
                    Log.i("测试4", message1);
                    try {
                        JSONObject json = new JSONObject(message1);

                        if (json.getLong("id") != mID) {
                            String messageType = json.getString("isimg"); // 根据isimg判断消息类型
                            String msgContent = json.getString("msg");
                            long times = json.getLong("times");
                            long id = json.getLong("id");
                            String username = json.optString("username", "unknown"); // 解析用户名

                            // 根据是否为图片创建MessageInfor对象，现在包括username参数
                            MessageInfo m = new MessageInfo(msgContent, times, id, messageType, username);

                            // 由于涉及UI更新，确保在主线程执行
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    datas.add(m);
                                    messageAdapter.notifyDataSetChanged(); // 通知数据源发生变化
                                }
                            });

                            titleText = json.optString("peoplen", "当前在线人数[未知]");
                            handler.sendEmptyMessage(1); // 可能需要更新UI，例如标题
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    message1 = br.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 发送消息
     */
    @SuppressLint("DefaultLocale")
    private void sendMsgText() {
        message = sendMessageText.getText().toString();
        if (message == null || "".equals(message)) {
            Toast.makeText(ChatRoomActivity.this, "发送消息不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        //从SharedPreferences获取用户名
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "unknown");

        long Ltimes = System.currentTimeMillis();
        MessageInfo m = new MessageInfo(message, Ltimes, mID, "1", username);
        //MessageInfor m = new MessageInfor(message, Ltimes, mID, "1");//消息 时间戳 id

        //userSendMsg = "{\"isimg\":\"1\",\"msg\":\"" + sendMessageText.getText().toString() + "\",\"times\":\"" + Ltimes + "\",\"id\":\"" + mID + "\"}";
        // 在发送的JSON消息中包含username
        userSendMsg = String.format("{\"isimg\":\"1\",\"msg\":\"%s\",\"times\":\"%d\",\"id\":\"%d\", \"username\":\"%s\"}",
                message, Ltimes, mID, username);

        datas.add(m);
        messageAdapter.notifyDataSetChanged();//通知数据源发生变化

        sendMessageText.setText("");
    }


    class MessageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public MessageInfo getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            Long id = datas.get(i).getUserID();
            return id == null ? 0 : id;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MessageHolder holder = null;

            //初始化holder
            if (view == null) {
                view = LayoutInflater.from(ChatRoomActivity.this).inflate(R.layout.chat_item, null);
                holder = new MessageHolder();
                holder.init(view);  // 初始化控件
                view.setTag(holder);
            } else {
                holder = (MessageHolder) view.getTag();
            }

            MessageInfo mi = getItem(i);

            holder.resetViews();

            // 设置视图内容
            if (mi.getUserID() == mID) {  // 自己的消息
                setupMyMessage(holder, mi);
            } else {  // 别人的消息
                setupOtherMessage(holder, mi);
            }

            return view;
        }

        private void setupMyMessage(MessageHolder holder, MessageInfo mi) {
            holder.usernameRight.setText(mi.getUsername());
            holder.avatarRight.setImageResource(R.drawable.cc_face);

            if (mi.getType().equals("0")) {  // 图片
                holder.rightTime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                holder.rightTime.setVisibility(View.VISIBLE);
                holder.rightImg.setImageBitmap(convertStringToIcon(mi.getMsg()));
                holder.rightImg.setVisibility(View.VISIBLE);
            } else {  // 文字
                holder.right.setText(mi.getMsg());
                holder.rightTime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                holder.right.setVisibility(View.VISIBLE);
                holder.rightTime.setVisibility(View.VISIBLE);
            }

            holder.usernameRight.setVisibility(View.VISIBLE);
            holder.avatarRight.setVisibility(View.VISIBLE);
        }

        private void setupOtherMessage(MessageHolder holder, MessageInfo mi) {
            holder.usernameLeft.setText(mi.getUsername());
            holder.avatarLeft.setImageResource(R.drawable.cc_face);

            if (mi.getType().equals("0")) {  // 图片
                holder.leftImg.setImageBitmap(convertStringToIcon(mi.getMsg()));
                holder.leftTime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                holder.leftImg.setVisibility(View.VISIBLE);
                holder.leftTime.setVisibility(View.VISIBLE);
            } else {  // 文字
                holder.left.setText(mi.getMsg());
                holder.leftTime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                holder.left.setVisibility(View.VISIBLE);
                holder.leftTime.setVisibility(View.VISIBLE);
            }

            holder.usernameLeft.setVisibility(View.VISIBLE);
            holder.avatarLeft.setVisibility(View.VISIBLE);
        }
    }


    class MessageHolder {
        public TextView left; // 左侧消息文本
        public TextView right; // 右侧消息文本
        public TextView leftTime; // 左侧消息时间
        public TextView rightTime; // 右侧消息时间
        public TextView rightImgTime; // 右侧图片消息时间
        public TextView leftImgTime; // 左侧图片消息时间
        public ImageView rightImg; // 右侧图片
        public ImageView leftImg; // 左侧图片

        public ImageView avatarRight; // 右侧头像
        public ImageView avatarLeft; // 左侧头像
        public TextView usernameRight; // 右侧用户名
        public TextView usernameLeft; // 左侧用户名


        public void init(View view) {
            // 使用view.findViewById来初始化每个控件
            left = (TextView) view.findViewById(R.id.itemleft);
            right = (TextView) view.findViewById(R.id.itemright);
            leftTime = (TextView) view.findViewById(R.id.itemtimeleft);
            rightTime = (TextView) view.findViewById(R.id.itemtimeright);
            leftImgTime = (TextView) view.findViewById(R.id.leftimgtime);
            rightImgTime = (TextView) view.findViewById(R.id.rightimgtime);
            leftImg = (ImageView) view.findViewById(R.id.leftimg);
            rightImg = (ImageView) view.findViewById(R.id.rightimg);
            avatarRight = (ImageView) view.findViewById(R.id.avatarRight);
            avatarLeft = (ImageView) view.findViewById(R.id.avatarLeft);
            usernameRight = (TextView) view.findViewById(R.id.usernameRight);
            usernameLeft = (TextView) view.findViewById(R.id.usernameLeft);
        }

        public void resetViews() {
            // 控件的显示状态重置
            left.setVisibility(View.GONE);
            right.setVisibility(View.GONE);
            rightImg.setVisibility(View.GONE);
            leftImg.setVisibility(View.GONE);
            leftTime.setVisibility(View.GONE);
            rightTime.setVisibility(View.GONE);
            rightImgTime.setVisibility(View.GONE);
            leftImgTime.setVisibility(View.GONE);
            usernameRight.setVisibility(View.GONE);
            usernameLeft.setVisibility(View.GONE);
            avatarRight.setVisibility(View.GONE);
            avatarLeft.setVisibility(View.GONE);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                activityImage(selectedImage);
            } else {
                Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 处理图片发送
     */
    @SuppressLint("DefaultLocale")
    private void activityImage(Uri imageUri) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "unknown");
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            if (bm != null) {
                bm = resizeBitmap(bm, 400, 400, true);
                long Ltimes = System.currentTimeMillis();
                String imgString = convertIconToString(bm);
                imgString = imgString.replace("\n", "");

                datas.add(new MessageInfo(imgString, Ltimes, mID, "0", username));

                if (isServer) {
                    sendMessage(String.format("{\"isimg\":\"0\",\"msg\":\"%s\",\"times\":\"%d\",\"id\":\"%d\", \"username\":\"%s\"}",
                            imgString, Ltimes, mID, username));
                } else {
                    userSendMsg = String.format("{\"isimg\":\"0\",\"msg\":\"%s\",\"times\":\"%d\",\"id\":\"%d\", \"username\":\"%s\"}",
                            imgString, Ltimes, mID, username);

                    //userSendMsgQueue.add(userSendMsg);
                }
            } else {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "错误读取图片", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    private String convertIconToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }


    /**
     * string转成bitmap
     *
     * @param st
     * @return
     */
    private Bitmap convertStringToIcon(String st) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 处理图片
     *
     * @param bitmap    图片bitmap
     * @param MaxWidth  最大长
     * @param MaxHeight 最大宽
     * @param filter    是否过滤
     * @return 处理后的bitmap
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int MaxWidth, int MaxHeight, boolean filter) {
        Float ScalingNumber;
        Bitmap reBitmap;
        Matrix matrix = new Matrix();
        ScalingNumber = Float.valueOf(scalingNumber(bitmap.getWidth(), bitmap.getHeight(), MaxWidth, MaxHeight));
        matrix.setScale(1 / ScalingNumber, 1 / ScalingNumber);
        reBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, filter);

        return reBitmap;
    }


    /**
     * 计算缩放比例
     *
     * @param oldWidth  原长
     * @param oldHeight 原宽
     * @param MaxWidth  最大长
     * @param MaxHeight 最大宽
     * @return 缩放比系数
     */
    private int scalingNumber(int oldWidth, int oldHeight, int MaxWidth, int MaxHeight) {
        int scalingN = 1;
        if (oldWidth > MaxWidth || oldHeight > MaxHeight) {
            scalingN = 2;
            while ((oldWidth / scalingN > MaxWidth) || (oldHeight / scalingN > MaxHeight)) {
                scalingN *= 2;
            }
        }

        return scalingN;
    }


}