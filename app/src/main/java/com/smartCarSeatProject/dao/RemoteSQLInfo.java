package com.smartCarSeatProject.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RemoteSQLInfo {

    //远程数据库账号
    public static final String SQLITEURL = "jdbc:mysql://47.101.160.149:3306/nbsmartdb";// ip地址:3306 数据库名"
    //远程数据库账号
    public static final String SQLITEUSER = "root";
    //远程数据库密码
    public static final String SQLITEPW = "nbserver";
    public static Connection connection;
    static Statement statement = null;

    public RemoteSQLInfo() {
        if (statement == null) {
            // 连接远程服务器
            connectRemoteSQL();
        }
    }

    private void connectRemoteSQL() {
        //新建驱动，注册驱动
        try {
            if (connection == null) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                connection = (Connection) DriverManager.getConnection(SQLITEURL,SQLITEUSER, SQLITEPW);
            }
            statement = (Statement) connection.createStatement();
//            insertDataByDevelopData(new DevelopDataInfo());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String insertDataByDevelopData(DevelopDataInfo developDataInfo) {

        String strID = developDataInfo.getIID()+"";
        // 名称
        String strName = developDataInfo.getStrName();
        // 初始化A面8组气压
        String p_init_back_A = developDataInfo.getP_init_back_A();
        // 初始化靠背B面5组气压
        String p_init_back_B = developDataInfo.getP_init_back_B();
        // 初始化坐垫3组气压
        String p_init_cushion = developDataInfo.getP_init_cushion();

        // 初始化座垫传感器的值
        String p_init_cushion_A1 = developDataInfo.getP_init_cushion_A1();
        String p_init_cushion_A2 = developDataInfo.getP_init_cushion_A2();
        String p_init_cushion_B1 = developDataInfo.getP_init_cushion_B1();
        String p_init_cushion_B2 = developDataInfo.getP_init_cushion_B2();
        String p_init_cushion_C1 = developDataInfo.getP_init_cushion_C1();
        String p_init_cushion_C2 = developDataInfo.getP_init_cushion_C2();


        // 识别后靠背A面8组
        String p_recog_back_A = developDataInfo.getP_recog_back_A();
        String p_recog_back_B = developDataInfo.getP_recog_back_B();
        String p_recog_back_C = developDataInfo.getP_recog_back_C();
        String p_recog_back_D = developDataInfo.getP_recog_back_D();
        String p_recog_back_E = developDataInfo.getP_recog_back_E();
        String p_recog_back_F = developDataInfo.getP_recog_back_F();
        String p_recog_back_G = developDataInfo.getP_recog_back_G();
        String p_recog_back_H = developDataInfo.getP_recog_back_H();

        // 识别后坐垫3组
        String p_recog_cushion_6= developDataInfo.getP_recog_cushion_6();
        String p_recog_cushion_7 = developDataInfo.getP_recog_cushion_7();
        String p_recog_cushion_8 = developDataInfo.getP_recog_cushion_8();

        String p_recog_cushion_A1 = developDataInfo.getP_recog_cushion_A1();
        String p_recog_cushion_A2 = developDataInfo.getP_recog_cushion_A2();
        String p_recog_cushion_B1 = developDataInfo.getP_recog_cushion_B1();
        String p_recog_cushion_B2 = developDataInfo.getP_recog_cushion_B2();
        String p_recog_cushion_C1 = developDataInfo.getP_recog_cushion_C1();
        String p_recog_cushion_C2 = developDataInfo.getP_recog_cushion_C2();

        // 识别后靠背B面5组
        String p_recog_back_1 = developDataInfo.getP_recog_back_1();
        String p_recog_back_2 = developDataInfo.getP_recog_back_2();
        String p_recog_back_3 = developDataInfo.getP_recog_back_3();
        String p_recog_back_4 = developDataInfo.getP_recog_back_4();
        String p_recog_back_5 = developDataInfo.getP_recog_back_5();

        // 调节后坐垫3组
        String p_adjust_cushion_6 = developDataInfo.getP_adjust_cushion_6();
        String p_adjust_cushion_7 = developDataInfo.getP_adjust_cushion_7();
        String p_adjust_cushion_8 = developDataInfo.getP_adjust_cushion_8();

        // 调节后靠背B面5组
        String p_adjust_cushion_1 = developDataInfo.getP_adjust_cushion_1();
        String p_adjust_cushion_2 = developDataInfo.getP_adjust_cushion_2();
        String p_adjust_cushion_3 = developDataInfo.getP_adjust_cushion_3();
        String p_adjust_cushion_4 = developDataInfo.getP_adjust_cushion_4();
        String p_adjust_cushion_5 = developDataInfo.getP_adjust_cushion_5();

        // 人员-性别
        String m_gender =  developDataInfo.getM_gender();
        // 人员-国别
        String m_national = developDataInfo.getM_national();
        // 人员-体重
        String m_weight = developDataInfo.getM_weight();
        // 人员-身高
        String m_height = developDataInfo.getM_height();
        // 备注
        String strPSInfo= developDataInfo.getStrPSInfo();
        // 时间
        String saveTime = developDataInfo.getSaveTime();
        // 数据类型
        String dataType = developDataInfo.getDataType();
        // 位置调节
        String locationCtr = developDataInfo.l_location;
        // 心率
        String HeartRate = developDataInfo.getHeartRate();
        // 呼吸率
        String BreathRate = developDataInfo.getBreathRate();
        // 情绪值
        String E_Index = developDataInfo.getE_Index();
        // 舒张压
        String Dia_BP = developDataInfo.getDia_BP();
        // 收缩压
        String Sys_BP = developDataInfo.getSys_BP();
        // 信噪比
        String snr = developDataInfo.getSnr();

        // insert into table1(field1,field2) values(value1,value2)
        String sqlLeft="insert into "+DBContent.DeviceInfo.TABLE_NAME_Develop+"(" + DBContent.DeviceInfo.Columns.id+","+
                DBContent.DeviceInfo.Columns.dataName+","+ DBContent.DeviceInfo.Columns.p_init_back_A+","+ DBContent.DeviceInfo.Columns.p_init_back_B+","+ DBContent.DeviceInfo.Columns.p_init_cushion+","+
                DBContent.DeviceInfo.Columns.p_init_cushion_A1+","+ DBContent.DeviceInfo.Columns.p_init_cushion_A2+","+
                DBContent.DeviceInfo.Columns.p_init_cushion_B1+","+ DBContent.DeviceInfo.Columns.p_init_cushion_B2+","+
                DBContent.DeviceInfo.Columns.p_init_cushion_C1+","+ DBContent.DeviceInfo.Columns.p_init_cushion_C2+","+
                DBContent.DeviceInfo.Columns.p_recog_back_A+","+ DBContent.DeviceInfo.Columns.p_recog_back_B+","+ DBContent.DeviceInfo.Columns.p_recog_back_C+","+ DBContent.DeviceInfo.Columns.p_recog_back_D+","+
                DBContent.DeviceInfo.Columns.p_recog_back_E+","+ DBContent.DeviceInfo.Columns.p_recog_back_F+","+ DBContent.DeviceInfo.Columns.p_recog_back_G+","+ DBContent.DeviceInfo.Columns.p_recog_back_H+","+
                DBContent.DeviceInfo.Columns.p_recog_back_1+","+ DBContent.DeviceInfo.Columns.p_recog_back_2+","+ DBContent.DeviceInfo.Columns.p_recog_back_3+","+ DBContent.DeviceInfo.Columns.p_recog_back_4+","+
                DBContent.DeviceInfo.Columns.p_recog_back_5+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_6+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_7+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_8+","+
                DBContent.DeviceInfo.Columns.p_recog_cushion_A1+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_A2+","+
                DBContent.DeviceInfo.Columns.p_recog_cushion_B1+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_B2+","+
                DBContent.DeviceInfo.Columns.p_recog_cushion_C1+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_C2+","+
                DBContent.DeviceInfo.Columns.p_adjust_cushion_1+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_2+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_3+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_4+","+
                DBContent.DeviceInfo.Columns.p_adjust_cushion_5+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_6+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_7+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_8+","+
                DBContent.DeviceInfo.Columns.m_gender+","+ DBContent.DeviceInfo.Columns.m_national+","+
                DBContent.DeviceInfo.Columns.m_weight+","+ DBContent.DeviceInfo.Columns.m_height+","+
                DBContent.DeviceInfo.Columns.strPSInfo+","+ DBContent.DeviceInfo.Columns.saveTime+","+
                DBContent.DeviceInfo.Columns.dataType+","+ DBContent.DeviceInfo.Columns.loactionCtr+","+
                DBContent.DeviceInfo.Columns.HeartRate+","+ DBContent.DeviceInfo.Columns.BreathRate+","+
                DBContent.DeviceInfo.Columns.E_Index+","+ DBContent.DeviceInfo.Columns.Dia_BP+","+
                DBContent.DeviceInfo.Columns.Sys_BP+","+ DBContent.DeviceInfo.Columns.Snr+ ")";
        String sqlRight = "values('"+strID+"','"+strName+"','"+p_init_back_A+"','"+p_init_back_B+"','"+p_init_cushion+"','"+
                p_init_cushion_A1+"','"+ p_init_cushion_A2+"','"+
                p_init_cushion_B1+"','"+p_init_cushion_B2+"','"+
                p_init_cushion_C1+"','"+p_init_cushion_C2+"','"+
                p_recog_back_A+"','"+ p_recog_back_B+"','"+p_recog_back_C+"','"+p_recog_back_D+"','"+
                p_recog_back_E+"','"+p_recog_back_F+"','"+ p_recog_back_G+"','"+ p_recog_back_H+"','"+
                p_recog_back_1+"','"+ p_recog_back_2+"','"+p_recog_back_3+"','"+ p_recog_back_4+"','"+
                p_recog_back_5+"','"+ p_recog_cushion_6+"','"+p_recog_cushion_7+"','"+p_recog_cushion_8+"','"+
                p_recog_cushion_A1+"','"+p_recog_cushion_A2+"','"+
                p_recog_cushion_B1+"','"+ p_recog_cushion_B2+"','"+
                p_recog_cushion_C1+"','"+p_recog_cushion_C2+"','"+
                p_adjust_cushion_1+"','"+ p_adjust_cushion_2+"','"+p_adjust_cushion_3+"','"+ p_adjust_cushion_4+"','"+
                p_adjust_cushion_5+"','"+ p_adjust_cushion_6+"','"+p_adjust_cushion_7+"','"+p_adjust_cushion_8+"','"+
                m_gender+"','"+ m_national+"','"+
                m_weight+"','"+ m_height+"','"+
                strPSInfo+"','"+saveTime+"','"+
                dataType+"','"+ locationCtr+"','"+
                HeartRate+"','"+BreathRate+"','"+
                E_Index+"','"+Dia_BP+"','"+
                Sys_BP+"','"+ snr+"')";
        String strSql = sqlLeft+sqlRight;
        try {
             int iResult = statement.executeUpdate(strSql);
             if (iResult != 1)
                 return "Cloud add failure：code:"+iResult;
             else
                 return "";

        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage().toString();
        }


    }


    public String insertDataByManualData(DevelopDataInfo developDataInfo) {

        String strID = developDataInfo.getIID()+"";
        // 名称
        String strName = developDataInfo.getStrName();
        // 初始化A面8组气压
        String p_init_back_A = developDataInfo.getP_init_back_A();
        // 初始化靠背B面5组气压
        String p_init_back_B = developDataInfo.getP_init_back_B();
        // 初始化坐垫3组气压
        String p_init_cushion = developDataInfo.getP_init_cushion();

        // 初始化座垫传感器的值
        String p_init_cushion_A1 = developDataInfo.getP_init_cushion_A1();
        String p_init_cushion_A2 = developDataInfo.getP_init_cushion_A2();
        String p_init_cushion_B1 = developDataInfo.getP_init_cushion_B1();
        String p_init_cushion_B2 = developDataInfo.getP_init_cushion_B2();
        String p_init_cushion_C1 = developDataInfo.getP_init_cushion_C1();
        String p_init_cushion_C2 = developDataInfo.getP_init_cushion_C2();


        // 识别后靠背A面8组
        String p_recog_back_A = developDataInfo.getP_recog_back_A();
        String p_recog_back_B = developDataInfo.getP_recog_back_B();
        String p_recog_back_C = developDataInfo.getP_recog_back_C();
        String p_recog_back_D = developDataInfo.getP_recog_back_D();
        String p_recog_back_E = developDataInfo.getP_recog_back_E();
        String p_recog_back_F = developDataInfo.getP_recog_back_F();
        String p_recog_back_G = developDataInfo.getP_recog_back_G();
        String p_recog_back_H = developDataInfo.getP_recog_back_H();

        // 识别后坐垫3组
        String p_recog_cushion_6= developDataInfo.getP_recog_cushion_6();
        String p_recog_cushion_7 = developDataInfo.getP_recog_cushion_7();
        String p_recog_cushion_8 = developDataInfo.getP_recog_cushion_8();

        String p_recog_cushion_A1 = developDataInfo.getP_recog_cushion_A1();
        String p_recog_cushion_A2 = developDataInfo.getP_recog_cushion_A2();
        String p_recog_cushion_B1 = developDataInfo.getP_recog_cushion_B1();
        String p_recog_cushion_B2 = developDataInfo.getP_recog_cushion_B2();
        String p_recog_cushion_C1 = developDataInfo.getP_recog_cushion_C1();
        String p_recog_cushion_C2 = developDataInfo.getP_recog_cushion_C2();

        // 识别后靠背B面5组
        String p_recog_back_1 = developDataInfo.getP_recog_back_1();
        String p_recog_back_2 = developDataInfo.getP_recog_back_2();
        String p_recog_back_3 = developDataInfo.getP_recog_back_3();
        String p_recog_back_4 = developDataInfo.getP_recog_back_4();
        String p_recog_back_5 = developDataInfo.getP_recog_back_5();

        // 调节后坐垫3组
        String p_adjust_cushion_6 = developDataInfo.getP_adjust_cushion_6();
        String p_adjust_cushion_7 = developDataInfo.getP_adjust_cushion_7();
        String p_adjust_cushion_8 = developDataInfo.getP_adjust_cushion_8();

        // 调节后靠背B面5组
        String p_adjust_cushion_1 = developDataInfo.getP_adjust_cushion_1();
        String p_adjust_cushion_2 = developDataInfo.getP_adjust_cushion_2();
        String p_adjust_cushion_3 = developDataInfo.getP_adjust_cushion_3();
        String p_adjust_cushion_4 = developDataInfo.getP_adjust_cushion_4();
        String p_adjust_cushion_5 = developDataInfo.getP_adjust_cushion_5();

        // 人员-性别
        String m_gender =  developDataInfo.getM_gender();
        // 人员-国别
        String m_national = developDataInfo.getM_national();
        // 人员-体重
        String m_weight = developDataInfo.getM_weight();
        // 人员-身高
        String m_height = developDataInfo.getM_height();
        // 备注
        String strPSInfo= developDataInfo.getStrPSInfo();
        // 时间
        String saveTime = developDataInfo.getSaveTime();
        // 数据类型
        String dataType = developDataInfo.getDataType();
        // 位置调节
        String locationCtr = developDataInfo.l_location;
        // 心率
        String HeartRate = developDataInfo.getHeartRate();
        // 呼吸率
        String BreathRate = developDataInfo.getBreathRate();
        // 情绪值
        String E_Index = developDataInfo.getE_Index();
        // 舒张压
        String Dia_BP = developDataInfo.getDia_BP();
        // 收缩压
        String Sys_BP = developDataInfo.getSys_BP();
        // 信噪比
        String snr = developDataInfo.getSnr();

        String sqlLeft="insert into "+DBContent.DeviceInfo.TABLE_NAME_Manual+"(" + DBContent.DeviceInfo.Columns.id+","+
                DBContent.DeviceInfo.Columns.dataName+","+ DBContent.DeviceInfo.Columns.p_init_back_A+","+ DBContent.DeviceInfo.Columns.p_init_back_B+","+ DBContent.DeviceInfo.Columns.p_init_cushion+","+
                DBContent.DeviceInfo.Columns.p_init_cushion_A1+","+ DBContent.DeviceInfo.Columns.p_init_cushion_A2+","+
                DBContent.DeviceInfo.Columns.p_init_cushion_B1+","+ DBContent.DeviceInfo.Columns.p_init_cushion_B2+","+
                DBContent.DeviceInfo.Columns.p_init_cushion_C1+","+ DBContent.DeviceInfo.Columns.p_init_cushion_C2+","+
                DBContent.DeviceInfo.Columns.p_recog_back_A+","+ DBContent.DeviceInfo.Columns.p_recog_back_B+","+ DBContent.DeviceInfo.Columns.p_recog_back_C+","+ DBContent.DeviceInfo.Columns.p_recog_back_D+","+
                DBContent.DeviceInfo.Columns.p_recog_back_E+","+ DBContent.DeviceInfo.Columns.p_recog_back_F+","+ DBContent.DeviceInfo.Columns.p_recog_back_G+","+ DBContent.DeviceInfo.Columns.p_recog_back_H+","+
                DBContent.DeviceInfo.Columns.p_recog_back_1+","+ DBContent.DeviceInfo.Columns.p_recog_back_2+","+ DBContent.DeviceInfo.Columns.p_recog_back_3+","+ DBContent.DeviceInfo.Columns.p_recog_back_4+","+
                DBContent.DeviceInfo.Columns.p_recog_back_5+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_6+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_7+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_8+","+
                DBContent.DeviceInfo.Columns.p_recog_cushion_A1+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_A2+","+
                DBContent.DeviceInfo.Columns.p_recog_cushion_B1+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_B2+","+
                DBContent.DeviceInfo.Columns.p_recog_cushion_C1+","+ DBContent.DeviceInfo.Columns.p_recog_cushion_C2+","+
                DBContent.DeviceInfo.Columns.p_adjust_cushion_1+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_2+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_3+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_4+","+
                DBContent.DeviceInfo.Columns.p_adjust_cushion_5+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_6+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_7+","+ DBContent.DeviceInfo.Columns.p_adjust_cushion_8+","+
                DBContent.DeviceInfo.Columns.m_gender+","+ DBContent.DeviceInfo.Columns.m_national+","+
                DBContent.DeviceInfo.Columns.m_weight+","+ DBContent.DeviceInfo.Columns.m_height+","+
                DBContent.DeviceInfo.Columns.strPSInfo+","+ DBContent.DeviceInfo.Columns.saveTime+","+
                DBContent.DeviceInfo.Columns.dataType+","+ DBContent.DeviceInfo.Columns.loactionCtr+","+
                DBContent.DeviceInfo.Columns.HeartRate+","+ DBContent.DeviceInfo.Columns.BreathRate+","+
                DBContent.DeviceInfo.Columns.E_Index+","+ DBContent.DeviceInfo.Columns.Dia_BP+","+
                DBContent.DeviceInfo.Columns.Sys_BP+","+ DBContent.DeviceInfo.Columns.Snr+ ")";
        String sqlRight = "values('"+strID+"','"+strName+"','"+p_init_back_A+"','"+p_init_back_B+"','"+p_init_cushion+"','"+
                p_init_cushion_A1+"','"+ p_init_cushion_A2+"','"+
                p_init_cushion_B1+"','"+p_init_cushion_B2+"','"+
                p_init_cushion_C1+"','"+p_init_cushion_C2+"','"+
                p_recog_back_A+"','"+ p_recog_back_B+"','"+p_recog_back_C+"','"+p_recog_back_D+"','"+
                p_recog_back_E+"','"+p_recog_back_F+"','"+ p_recog_back_G+"','"+ p_recog_back_H+"','"+
                p_recog_back_1+"','"+ p_recog_back_2+"','"+p_recog_back_3+"','"+ p_recog_back_4+"','"+
                p_recog_back_5+"','"+ p_recog_cushion_6+"','"+p_recog_cushion_7+"','"+p_recog_cushion_8+"','"+
                p_recog_cushion_A1+"','"+p_recog_cushion_A2+"','"+
                p_recog_cushion_B1+"','"+ p_recog_cushion_B2+"','"+
                p_recog_cushion_C1+"','"+p_recog_cushion_C2+"','"+
                p_adjust_cushion_1+"','"+ p_adjust_cushion_2+"','"+p_adjust_cushion_3+"','"+ p_adjust_cushion_4+"','"+
                p_adjust_cushion_5+"','"+ p_adjust_cushion_6+"','"+p_adjust_cushion_7+"','"+p_adjust_cushion_8+"','"+
                m_gender+"','"+ m_national+"','"+
                m_weight+"','"+ m_height+"','"+
                strPSInfo+"','"+saveTime+"','"+
                dataType+"','"+ locationCtr+"','"+
                HeartRate+"','"+BreathRate+"','"+
                E_Index+"','"+Dia_BP+"','"+
                Sys_BP+"','"+ snr+"')";
        String strSql = sqlLeft+sqlRight;
        try {
            int iResult = statement.executeUpdate(strSql);
            if (iResult != 1)
                return "Cloud add failure：code:"+iResult;
            else
                return "";

        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage().toString();
        }
    }


//    选择：select * from table1 where 范围
//    插入：insert into table1(field1,field2) values(value1,value2)
//    删除：delete from table1 where 范围
//    更新：update table1 set field1=value1 where 范围
//    查找：select * from table1 where field1 like ’%value1%’ ---like的语法很精妙，查资料!

    public void cloaseRemoteSQL() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
