package com.smartCarSeatProject.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.smartCarSeatProject.data.ControlPressInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private String DB_NAME = "body_press.db";
    private Context mContext;
    private SQLiteDatabase sqLiteDatabase = null;

    public DBManager(Context mContext) {
        this.mContext = mContext;
        String strPackName = mContext.getPackageName();
        String dbPath = "/data/data/" + strPackName + "/databases/" + DB_NAME;
        File jhPath=new File(dbPath);
        if (!jhPath.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(dbPath);
                InputStream in = mContext.getAssets().open("body_press.db");
                byte[] buffer = new byte[1024];
                int readBytes = 0;
                while ((readBytes = in.read(buffer)) != -1)
                    out.write(buffer, 0, readBytes);
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbPath, null);

    }

    String[] columns = new String[]{"weight", "press1", "press2", "press3", "press4", "press5", "press6", "press7", "press8"};
    String selection = "weight >= ?";
    //查询
    public List<ControlPressInfo> queryLikeWeight(String strTableName, double iValue) {
        List<ControlPressInfo> list = new ArrayList<>();
        String[] selectionArgs = {iValue+""};
        try {
            Cursor cursor = sqLiteDatabase.query(strTableName, columns, selection, selectionArgs, null, null, "weight asc");// 按照身高，升序排列，第一个就是最接近的
            while (cursor != null && cursor.moveToNext()) {
                int iWeight = cursor.getInt(cursor.getColumnIndex("weight"));
                int iPress1 = cursor.getInt(cursor.getColumnIndex("press1"));
                int iPress2 = cursor.getInt(cursor.getColumnIndex("press2"));
                int iPress3 = cursor.getInt(cursor.getColumnIndex("press3"));
                int iPress4 = cursor.getInt(cursor.getColumnIndex("press4"));
                int iPress5 = cursor.getInt(cursor.getColumnIndex("press5"));
                int iPress6 = cursor.getInt(cursor.getColumnIndex("press6"));
                int iPress7 = cursor.getInt(cursor.getColumnIndex("press7"));
                int iPress8 = cursor.getInt(cursor.getColumnIndex("press8"));
                ControlPressInfo city = new ControlPressInfo(iWeight,iPress1,iPress2,iPress3,iPress4,iPress5,iPress6,iPress7,iPress8);
                list.add(city);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void closeDb() {
        sqLiteDatabase.close();
    }

}