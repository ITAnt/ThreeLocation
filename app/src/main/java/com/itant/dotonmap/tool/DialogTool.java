package com.itant.dotonmap.tool;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Jason on 2018/11/20.
 */

public class DialogTool {
    public static void showPermissionDialog(final Activity activity) {
        // 退出当前账号
        AlertDialog dialog = new AlertDialog.Builder(activity)
                //.setIcon(R.mipmap.icon)//设置标题的图片
                //.setTitle("我是对话框")//设置对话框的标题
                .setMessage("本应用的正常运行需要定位和存储等权限")//设置对话框的内容
                //设置对话框的按钮
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}
