package com.gjy.chatroom2.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.gjy.chatroom2.DbContect;
import com.gjy.chatroom2.R;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gjy.chatroom2.databinding.ActivityMainBinding;

import android.database.sqlite.SQLiteDatabase;
import com.gjy.chatroom2.javabean.MessageInfor;
import org.checkerframework.checker.units.qual.C;
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
    private String message = "", userSendMsg = "", titletext = "";
    private String[] ConnectServerData = new String[2];// 0.ipv4 1.端口号
    private Long mID = 0L;
    private List<MessageInfor> datas = new ArrayList<MessageInfor>();
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
                    titleView.setText(titletext);
                } else if (msg.what == 2) {
                    titleView.setText("当前在线人数[" + (allOut.size() + 1) + "]");
                }
                super.handleMessage(msg);
            }
        };

        //设置点击事件

        // 启动服务器按钮点击事件
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
                EditText editprot = (EditText) layout.findViewById(R.id.editprot);

                new AlertDialog.Builder(ChatRoomActivity.this)
                        .setView(layout)  // 设置显示内容
                        .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startPort = Integer.valueOf(editprot.getText().toString());
                                mID = System.currentTimeMillis();
                                ServerInit();
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
                                ConnectServerData[0] = editIpv4Text.getText().toString();
                                ConnectServerData[1] = editPortText.getText().toString();
                                Toast.makeText(ChatRoomActivity.this, "正在连接服务器" + ConnectServerData[0] + ":" + ConnectServerData[1]
                                        , Toast.LENGTH_SHORT).show();
                                ConnectSever();
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
                if (isServer) {
                    //服务器
                    message = sendMessageText.getText().toString();
                    if (message == null || "".equals(message)) {
                        Toast.makeText(ChatRoomActivity.this, "发送消息不能为空", Toast.LENGTH_LONG).show();
                        return;
                    }
                    long Ltimes = System.currentTimeMillis();
                    message = sendMessageText.getText().toString();
                    datas.add(new MessageInfor(message, Ltimes, mID, "1"));
                    sendMessage("{\"isimg\":\"1\",\"msg\":\"" + message + "\",\"times\":\"" + Ltimes + "\",\"id\":\"" + mID + "\",\"peoplen\":\"" + "当前在线人数[" + (allOut.size() + 1) + "]" + "\"}");

                    //清空输入框文字
                    sendMessageText.setText("");


                } else {
                    //客户端
                    sendMsgText();
                }
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
                        if (json.getString("isimg").equals("1")) {
                            //不为图片
                            datas.add(new MessageInfor(json.getString("msg"), Long.valueOf(json.getString("times")), Long.valueOf(json.getString("id")), "1"));
                        } else if (json.getString("isimg").equals("0")) {
                            //图片
                            datas.add(new MessageInfor(json.getString("msg"), Long.valueOf(json.getString("times")), Long.valueOf(json.getString("id")), "0"));
                        }
                        titletext = json.getString("peoplen");
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
                            socket = new Socket(ConnectServerData[0], Integer.valueOf(ConnectServerData[1]));
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
                            if (json.getString("isimg").equals("1")) {//不为图片
                                datas.add(new MessageInfor(json.getString("msg"), Long.valueOf(json.getString("times")), Long.valueOf(json.getString("id")), "1"));
                            } else if (json.getString("isimg").equals("0")) {//为图片
                                datas.add(new MessageInfor(json.getString("msg"), Long.valueOf(json.getString("times")), Long.valueOf(json.getString("id")), "0"));
                            }
                        }
                        titletext = json.getString("peoplen");
                        handler.sendEmptyMessage(1);
                        //messageAdapte.notifyDataSetChanged();//通知数据源发生变化
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
    private void sendMsgText() {
        message = sendMessageText.getText().toString();
        if (message == null || "".equals(message)) {
            Toast.makeText(ChatRoomActivity.this, "发送消息不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        long Ltimes = System.currentTimeMillis();
        MessageInfor m = new MessageInfor(message, Ltimes, mID, "1");//消息 时间戳 id
        userSendMsg = "{\"isimg\":\"1\",\"msg\":\"" + sendMessageText.getText().toString() + "\",\"times\":\"" + Ltimes + "\",\"id\":\"" + mID + "\"}";
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
        public MessageInfor getItem(int i) {
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
            if (view == null) {
                view = LayoutInflater.from(ChatRoomActivity.this).inflate(R.layout.chart_item, null);
                holder = new MessageHolder();
                holder.left = (TextView) view.findViewById(R.id.itemleft);
                holder.right = (TextView) view.findViewById(R.id.itemright);
                holder.lefttime = (TextView) view.findViewById(R.id.itemtimeleft);
                holder.righttime = (TextView) view.findViewById(R.id.itemtimeright);

                holder.rightimgtime = (TextView) view.findViewById(R.id.rightimgtime);
                holder.leftimgtime = (TextView) view.findViewById(R.id.leftimgtime);
                holder.rightimg = (ImageView) view.findViewById(R.id.rightimg);
                holder.leftimg = (ImageView) view.findViewById(R.id.leftimg);

                view.setTag(holder);
            } else {
                holder = (MessageHolder) view.getTag();
            }
            MessageInfor mi = getItem(i);
            //显示
            if (mi.getUserID() == mID) {
                //id相等
                if (mi.getType().equals("0")) {
                    //图片
                    holder.leftimg.setVisibility(View.GONE);
                    holder.leftimgtime.setVisibility(View.GONE);
                    holder.rightimg.setVisibility(View.VISIBLE);
                    holder.rightimgtime.setVisibility(View.VISIBLE);
                    holder.rightimg.setImageBitmap(convertStringToIcon(mi.getMsg()));
                    holder.rightimgtime.setText(simpleDateFormat.format(new Date(mi.getTime())));

                    holder.left.setVisibility(View.GONE);
                    holder.lefttime.setVisibility(View.GONE);
                    holder.right.setVisibility(View.GONE);
                    holder.righttime.setVisibility(View.GONE);

                } else if (mi.getType().equals("1")) {
                    //消息
                    holder.leftimg.setVisibility(View.GONE);
                    holder.leftimgtime.setVisibility(View.GONE);
                    holder.rightimg.setVisibility(View.GONE);
                    holder.rightimgtime.setVisibility(View.GONE);

                    holder.left.setVisibility(View.GONE);
                    holder.lefttime.setVisibility(View.GONE);
                    holder.right.setVisibility(View.VISIBLE);
                    holder.righttime.setVisibility(View.VISIBLE);
                    holder.right.setText(mi.getMsg());
                    holder.righttime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                }


            } else {
                if (mi.getType().equals("0")) {
                    //图片
                    holder.leftimg.setVisibility(View.VISIBLE);
                    holder.leftimgtime.setVisibility(View.VISIBLE);
                    holder.rightimg.setVisibility(View.GONE);
                    holder.rightimgtime.setVisibility(View.GONE);
                    holder.leftimg.setImageBitmap(convertStringToIcon(mi.getMsg()));
                    holder.leftimgtime.setText(simpleDateFormat.format(new Date(mi.getTime())));

                    holder.left.setVisibility(View.GONE);
                    holder.lefttime.setVisibility(View.GONE);
                    holder.right.setVisibility(View.GONE);
                    holder.righttime.setVisibility(View.GONE);

                } else if (mi.getType().equals("1")) {
                    //消息
                    holder.leftimg.setVisibility(View.GONE);
                    holder.leftimgtime.setVisibility(View.GONE);
                    holder.rightimg.setVisibility(View.GONE);
                    holder.rightimgtime.setVisibility(View.GONE);

                    holder.left.setVisibility(View.VISIBLE);
                    holder.lefttime.setVisibility(View.VISIBLE);
                    holder.right.setVisibility(View.GONE);
                    holder.righttime.setVisibility(View.GONE);
                    holder.left.setText(mi.getMsg());
                    holder.lefttime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                }
            }
            return view;
        }
    }


    class MessageHolder {
        public TextView left;
        public TextView right;
        public TextView lefttime;
        public TextView righttime;
        private TextView rightimgtime;
        private TextView leftimgtime;
        private ImageView rightimg;
        private ImageView leftimg;

    }


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //获取图片路径

        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);

            String imagePath = c.getString(columnIndex);

            activityImage(imagePath);
            c.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 处理图片发送
     *
     * @param imagePath 图片路径
     */
    private void activityImage(String imagePath) {

        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        bm = resizeBitmap(bm, 400, 400, true);
        long Ltimes = System.currentTimeMillis();
        String imgString = convertIconToString(bm);
        imgString = imgString.replace("\n", "");
        datas.add(new MessageInfor(imgString, Ltimes, mID, "0"));

        if (isServer) {
            //服务器
            sendMessage("{\"isimg\":\"0\",\"msg\":\"" + imgString + "\",\"times\":\"" + Ltimes + "\",\"id\":\"" + mID + "\"}");
        } else {
            //客户端
            userSendMsg = "{\"isimg\":\"0\",\"msg\":\"" + imgString + "\",\"times\":\"" + Ltimes + "\",\"id\":\"" + mID + "\"}";
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