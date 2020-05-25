package com.smartCarSeatProject.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBBaseDao {
	protected SQLiteDatabase mDb;

	public DBBaseDao(SQLiteDatabase db) {
		this.mDb = db;
	}

	/**
	 * 
	 * @param tabName
	 * @return
	 */
	public boolean tabIsExist(String tabName) {
		boolean result = false;
		if (tabName == null) {
			return false;
		}
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
					+ tabName.trim() + "' ";
			cursor = mDb.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
		}
		return result;
	}
	public int getCount(String sql){
		int result=0;
		Cursor cursor = null;
		try {
			cursor = mDb.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = count;
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * ���������鿴ĳ�������Ƿ����
	 * 
	 * @param table
	 * @param id
	 * @return
	 */
	public boolean isExistsById(String table, String primaryKey, String id) {
		return isExistsByField(table, primaryKey, id);
	}

	/**
	 * ����ĳ�ֶ�/ֵ�鿴ĳ�������Ƿ����
	 * @return
	 */
	public boolean isExistsByField(String table, String column, String value) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
				.append(column).append(" =?");

		return isExistsBySQL(sql.toString(), new String[] { value });
	}

	/**
	 * ʹ��SQL���鿴ĳ�������Ƿ����
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public boolean isExistsBySQL(String sql, String[] selectionArgs) {
		boolean result = false; 
		final Cursor c = mDb.rawQuery(sql, selectionArgs);
		try {
			if (c.moveToFirst()) {
				result = (c.getInt(0) > 0);
			}
		} finally {
			c.close();
		}
		return result;
	}
	
	/**
	 * 
	 * @param sql  sql���
	 * @param rowMapper   
	 * @param selectionArgs
	 * @return
	 */
	
	public <T> ArrayList<T> queryForListBySql(String sql  , SQLiteTemplate.RowMapper<T> rowMapper, String[] selectionArgs) {
		ArrayList<T> list = new ArrayList<T>();

		final Cursor c = mDb.rawQuery(sql, selectionArgs);
		try {
			while (c.moveToNext()) {
				list.add(rowMapper.mapRow(c, 1));
				
			}
		} finally {
			c.close();
		}
		return list;
	}
	

	/**
	 * Close Database
	 */
	public void close() {
		if (null != mDb) {
			mDb.close();
			mDb = null;
		}
	}
}
