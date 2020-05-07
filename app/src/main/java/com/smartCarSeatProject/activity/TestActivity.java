package com.smartCarSeatProject.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.smartCarSeatProject.R;
import com.smartCarSeatProject.dao.DBManager;
import com.smartCarSeatProject.data.ControlPressInfo;
import com.smartCarSeatProject.isometric.ColorM;
import com.smartCarSeatProject.isometric.Isometric;
import com.smartCarSeatProject.isometric.IsometricView;
import com.smartCarSeatProject.isometric.Point;
import com.smartCarSeatProject.isometric.shapes.Prism;
import org.jetbrains.annotations.Nullable;
import java.util.List;


public class TestActivity extends BaseActivity {

    IsometricView isometricView;

    double iBei = 0.5;
    public static ColorM colorMBai = new ColorM(255, 255, 255);
    public static ColorM colorM = new ColorM(5, 122, 205);
    public static ColorM colorMZi = new ColorM(162, 77, 245);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);

//        test0();


        test2();


    }

    public void test0() {
        isometricView = findViewById(R.id.isometricView);
        // point:坐标系， dx,dy,dz：图形大小，iTag：标记，translate：偏移量,colorM：颜色
        isometricView.add(new Prism(new Point(-0.8*iBei, 0.5*iBei, 0*iBei),1*iBei,1*iBei,0.3*iBei,0), colorM);
        isometricView.add(new Prism(new Point(-0.8*iBei, 1.8*iBei, 0*iBei),1*iBei,1*iBei,0.3*iBei,1), colorM);
        isometricView.add(new Prism(new Point(0.5*iBei, 0.5*iBei, 0*iBei),1*iBei,2.3*iBei,0.3*iBei,2), colorM);
        isometricView.add(new Prism(new Point(2.5*iBei, 0.5*iBei, -0.5*iBei),0.3*iBei,1*iBei,1*iBei,3), colorM);
        isometricView.add(new Prism(new Point(2.5*iBei, 1.8*iBei, -0.5*iBei),0.3*iBei,1*iBei,1*iBei,4), colorM);
        isometricView.add(new Prism(new Point(2.7*iBei, 0.5*iBei, 0.7*iBei),0.3*iBei,1*iBei,1*iBei,5), colorM);
        isometricView.add(new Prism(new Point(2.7*iBei, 1.8*iBei, 0.7*iBei),0.3*iBei,1*iBei,1*iBei,6), colorM);
        isometricView.add(new Prism(new Point(2.9*iBei, 0.5*iBei, 1.9*iBei),0.3*iBei,1*iBei,1*iBei,7), colorM);
        isometricView.add(new Prism(new Point(2.9*iBei, 1.8*iBei, 1.9*iBei),0.3*iBei,1*iBei,1*iBei,8), colorM);
        isometricView.add(new Prism(new Point(3.2*iBei, 0.5*iBei, 3.1*iBei),0.3*iBei,1*iBei,1*iBei,9), colorM);
        isometricView.add(new Prism(new Point(3.2*iBei, 1.8*iBei, 3.1*iBei),0.3*iBei,1*iBei,1*iBei,10), colorM);
        isometricView.add(new Prism(new Point(3.5*iBei, 0.5*iBei, 4.3*iBei),0.3*iBei,1*iBei,1*iBei,11), colorM);
        isometricView.add(new Prism(new Point(3.5*iBei, 1.8*iBei, 4.3*iBei),0.3*iBei,1*iBei,1*iBei,12), colorM);
        // 倾斜角度
        isometricView.setRotation(8);
        isometricView.setiTag(7788);


        isometricView.setClickListener(new IsometricView.OnItemClickListener() {
            @Override
            public void onClick(Isometric.Item item,int iTag) {
                Loge("",item.toString());
                Loge("",item.getOriginalShape().toString()
                );
                Loge("",item.getPath().toString());
                int iBuTag = item.getPath().getiTag();
                Loge("","选中图形的tag："+iBuTag);
                ToastMsg("座椅："+iTag+"，选中图形的tag："+iBuTag);

                isometricView.changeColorByTag(iBuTag,colorMZi);
                isometricView.invalidate();

            }
        });

        findViewById(R.id.btnBai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isometricView.changeColorByTag(4,colorMBai);
                isometricView.invalidate();
            }
        });

        findViewById(R.id.btnLan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isometricView.changeColorByTag(4,colorM);
                isometricView.invalidate();
            }
        });

        findViewById(R.id.btnZi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isometricView.changeColorByTag(4,colorMZi);
                isometricView.invalidate();
            }
        });
    }



    public void test2() {

        findViewById(R.id.btnData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTableName = "t_body_10";
                DBManager dbManager = new DBManager(getMContext());
                List<ControlPressInfo> city = dbManager.queryLikeWeight(strTableName,66);
                for (ControlPressInfo city1 : city) {
                    Log.e("test2", "test2: cityInof:"+city1.toString());
                }
                dbManager.closeDb();
            }
        });

    }

}
