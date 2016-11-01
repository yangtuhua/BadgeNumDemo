package com.badgenumdemo;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * 设置应用图标右上角未读消息数
 * Created by yangtufa on 16/10/12.
 */
public class BadgeNumUtil {
    private String lancherActivityClassName;
    private Context context;
    private static NotificationManagerCompat managerCompat;
    private int notificationId;//通知id


    public BadgeNumUtil(Context context,int notificationId) {
        this.context = context;
        this.notificationId=notificationId;

        lancherActivityClassName=getLauncherClassName(context);
        Log.e("info","加载入口类："+lancherActivityClassName);
    }

    public void sendBadgeNumber(int number) {
        number=Math.min(number,99);
        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
            Log.e("info","发送到小米");
            sendToXiaoMi(number);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            sendToSamsumg(number);
            Log.e("info","发送到三星");
        } else if (Build.MANUFACTURER.toLowerCase().contains("sony")) {
            sendToSony(number);
            Log.e("info","发送到索尼");
        }else if (Build.MANUFACTURER.toLowerCase().contains("huawei")) {
            sendToHuaiwei(number);
            Log.e("info","发送到华为");
        } else {
            //不支持
            Log.e("info","不支持该品牌手机");
        }
    }

    //vivo手机
    private void sendToVivo(int number){
        Intent localIntent1 = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
        localIntent1.putExtra("packageName", context.getPackageName());
        localIntent1.putExtra("className", MainActivity.class.getName());
        localIntent1.putExtra("notificationNum", number);
        context.sendBroadcast(localIntent1);
    }
    //TODO 华为手机,第三方应用无法获取Write_Settings权限，该权限只对系统内部应用开发，QQ微信也无法实现该功能
    private void sendToHuaiwei(int number){
        Bundle localBundle = new Bundle();
        localBundle.putString("package", context.getPackageName());
        localBundle.putString("class", lancherActivityClassName);
        localBundle.putInt("badgenumber", number);
        context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, localBundle);
    }

    //HTC
    private void sendToHTC(int number){
        try {
            Intent localIntent1 = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
            localIntent1.putExtra("packagename", context.getPackageName());
            localIntent1.putExtra("count", number);
            context.sendBroadcast(localIntent1);

            Intent localIntent2 = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
            ComponentName localComponentName = new ComponentName(context, lancherActivityClassName);
            localIntent2.putExtra("com.htc.launcher.extra.COMPONENT",localComponentName.flattenToShortString());
            localIntent2.putExtra("com.htc.launcher.extra.COUNT", 10);
            context.sendBroadcast(localIntent2);
        } catch (Exception localException) {
            Log.e("CHECK", "HTC : " + localException.getLocalizedMessage());
        }
    }

    //小米手机
    private void sendToXiaoMi(int number) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("小米角标")
                .setAutoCancel(true)
                .setContentText("miui桌面角标消息");

        managerCompat = NotificationManagerCompat.from(context);

        Notification notification = builder.build();
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");

            Object extraNotification = field.get(notification);

            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);

            method.invoke(extraNotification,number);//设置消息数
        } catch (Exception e) {
            e.printStackTrace();
        }
        managerCompat.notify(notificationId, notification);
    }

    //发送到索尼手机
    private void sendToSony(int number) {
        String activityName = getLauncherClassName(context);
        Log.e("info","索尼获取到的入口："+activityName);
        Toast.makeText(context,"索尼拿到的入口："+activityName,Toast.LENGTH_LONG).show();
        if (activityName == null){
            return;
        }
        Intent localIntent = new Intent();
        boolean isShow = true;
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", isShow);
        localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", activityName);
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", number);
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());
        context.sendBroadcast(localIntent);
        ShortcutBadger.applyCount(context,number);
    }

    //发送到samsung手机
    private void sendToSamsumg(int number) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", number);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }
    //获取入口类名
    private String getLauncherClassName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        // To limit the components this Intent will resolve to, by setting an
        // explicit package name.
        intent.setPackage(context.getPackageName());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // All Application must have 1 Activity at least.
        // Launcher activity must be found!
        ResolveInfo info = packageManager
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        // get a ResolveInfo containing ACTION_MAIN, CATEGORY_LAUNCHER
        // if there is no Activity which has filtered by CATEGORY_DEFAULT
        if (info == null) {
            info = packageManager.resolveActivity(intent, 0);
        }
        return info.activityInfo.name;
    }

    //清空num
    public void clearBadgeNum(){
        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            managerCompat.cancel(notificationId);
        }
        sendBadgeNumber(0);
    }
}
