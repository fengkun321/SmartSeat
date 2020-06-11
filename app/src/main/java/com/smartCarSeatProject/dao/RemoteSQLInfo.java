package com.smartCarSeatProject.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RemoteSQLInfo {

    //远程数据库账号
    public static final String SQLITEURL = "jdbc:mysql://47.101.160.149:3306/NBServer.db";// ip地址:3306 数据库名";
    //远程数据库账号
    public static final String SQLITEUSER = "root";
    //远程数据库密码
    public static final String SQLITEPW = " nbserver";
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
            connection = (Connection) DriverManager.getConnection(SQLITEURL, SQLITEUSER, SQLITEPW);
            if (connection == null) {
                connection = (Connection) DriverManager.getConnection(SQLITEURL,SQLITEUSER, SQLITEPW);
            }
            statement = (Statement) connection.createStatement();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData(DevelopDataInfo developDataInfo) {
//        String sql="insert into student values("+textOne.getText()+",\'"+textTwo.getText()+"\'"+",\'"+textThree.getText()+"\');";
        String sql="";
        try {
            statement.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
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
