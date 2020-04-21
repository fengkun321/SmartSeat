package com.smartCarSeatProject.activity;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.smartCarSeatProject.R;
import com.smartCarSeatProject.dao.DBManager;
import com.smartCarSeatProject.data.City;
import com.smartCarSeatProject.isometric.ColorM;
import com.smartCarSeatProject.isometric.Isometric;
import com.smartCarSeatProject.isometric.IsometricView;
import com.smartCarSeatProject.isometric.Point;
import com.smartCarSeatProject.isometric.shapes.Prism;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
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

        Test1();

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


    public void Test1() {
        String s = "";//lua脚本
        s += "x=3\r\n";
        s += "y=4\r\n";
        s += "print ('hello world!')\r\n";
        s += "function aa()\r\n";
        s += "print ('aaa')\r\n";
        s += "end\r\n";
        s += "aa()\r\n";
        s += "c=method1(x)\r\n";
        s += "d=test.method2(x,y)\r\n";
        s += "print (x..'π='..c)\r\n";
        s += "print ('x*y='..d)\r\n";

        Globals globals = JsePlatform.standardGlobals();//初始化lua
        globals.load(new TestMethod());//注入方法
        LuaValue chunk = globals.load(s);//加载自己写的脚本
        chunk.call();//执行脚本
        String d = globals.get("d").toString();//取得脚本里的变量d的值
        System.out.println("d:" + d);
    }

    public class TestMethod extends TwoArgFunction {
        /**
         * The implementation of the TwoArgFunction interface.
         * This will be called once when the library is loaded via require().
         *
         * @param modname LuaString containing the name used in the call to require().
         * @param env     LuaValue containing the environment for this function.
         * @return Value that will be returned in the require() call.  In this case,
         * it is the library itself.
         */
        public LuaValue call(LuaValue modname, LuaValue env) {
            //调用方式1 method1(x)
            env.set("method1", new Method1());
            env.set("method2", new Method2());

            //调用方式2 test.method1(x)
            LuaValue library = tableOf();
            library.set("method1", new Method1());
            library.set("method2", new Method2());
            env.set("test", library);
            return null;
        }

        //一个参数的方法
        class Method1 extends OneArgFunction {
            public LuaValue call(LuaValue x) {
                return LuaValue.valueOf(Math.PI * x.checkint());
            }
        }

        //两个参数的方法
        class Method2 extends TwoArgFunction {
            public LuaValue call(LuaValue x, LuaValue y) {
                return LuaValue.valueOf(x.checkint() * y.checkint());
            }
        }
    }

    public void test2() {

        findViewById(R.id.btnData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTableName = "t_body_10";
                DBManager dbManager = new DBManager(getMContext());
                List<City> city = dbManager.queryLikeWeight(strTableName,66);
                for (City city1 : city) {
                    Log.e("test2", "test2: cityInof:"+city1.toString());
                }
                dbManager.closeDb();
            }
        });

    }

}
