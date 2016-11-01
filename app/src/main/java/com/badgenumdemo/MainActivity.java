package com.badgenumdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private BadgeNumUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btSet= (Button) findViewById(R.id.bt_set);
        Button btClear= (Button) findViewById(R.id.bt_clear);
        Button btSend= (Button) findViewById(R.id.bt_send_notification);
        Button btClearNotification= (Button) findViewById(R.id.bt_clear_notification);


        btClearNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.cancel(10);
            }
        });

        //发送通知
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View v) {
                sendNotification();
            }
        });
        btSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setBadge(MainActivity.this,15);
//                ShortcutBadger.applyCount(MainActivity.this, 10);
                int random = new Random().nextInt(10000);
                Log.e("info","产生的随机数："+random);
                util=new BadgeNumUtil(MainActivity.this,random);

                util.sendBadgeNumber(23);
            }
        });

        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setBadge(MainActivity.this,0);

//                ShortcutBadger.removeCount(MainActivity.this); //for 1.1.4+

                util.clearBadgeNum();
            }
        });
    }

    private void sendNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("测试标题")//设置通知栏标题
                .setContentText("测试内容")
                .setNumber(23) //设置通知集合的数量
                .setTicker("测试通知来啦") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON

        mNotificationManager.notify(10,mBuilder.build());
    }
}
