package com.smartCarSeatProject.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteTemplate {
	/**
	 * Default Primary key
	 */
	protected String mPrimaryKey = "_id";

	/**
	 * SQLiteDatabase Open Helper
	 */
	protected SQLiteOpenHelper mDatabaseOpenHelper;

	/**
	 * Construct
	 * 
	 * @param databaseOpenHelper
	 */
	public SQLiteTemplate(SQLiteOpenHelper databaseOpenHelper) {
		mDatabaseOpenHelper = databaseOpenHelper;
	}

	/**
	 * Construct
	 * 
	 * @param databaseOpenHelper
	 * @param primaryKey
	 */
	public SQLiteTemplate(SQLiteOpenHelper databaseOpenHelper, String primaryKey) {
		this(databaseOpenHelper);
		setPrimaryKey(primaryKey);
	}

	/**
	 * ���ĳһ���ֶκ�ֵɾ��һ�����, �� name="jack"
	 * 
	 * @param table
	 * @param field
	 * @param value
	 * @return
	 */
	public int deleteByField(String table, String field, String value) {
		return getDb(true).delete(table, field + "=?", new String[] { value });
	}

	/**
	 * �������ɾ��һ�����
	 * 
	 * @param table
	 * @param id
	 * @return
	 */
	public int deleteById(String table, String id) {
		return deleteByField(table, mPrimaryKey, id);
	}

	/**
	 * ����������һ�����
	 * 
	 * @param table
	 * @param id
	 * @param values
	 * @return
	 */
	public int updateById(String mPrimaryKey, String table, String id, ContentValues values) {
		return getDb(true).update(table, values, mPrimaryKey + "=?",
				new String[] { id });
	}
	/***
	 * ���ĳ2���ֶ���Ϣ����һ������� 
	 * @param Column1   �ֶ�1���
	 * @param Column2  �ֶ�2���
	 * @param table   ����
	 * @param Column1Value  �ֶ�1��ֵ
	 * @param Column2Value  �ֶ�2��ֵ
	 * @param values        ����µ������
	 * @return
	 */
	public int updateByTwoColumns(String Column1, String Column2, String table,
                                  String Column1Value, String Column2Value, ContentValues values) {
		return getDb(true).update(table, values, Column1 + "=? AND " + Column2 +"=?" ,
				new String[] { Column1Value, Column2Value});
	}

     


	/**
	 * �������鿴ĳ������Ƿ����
	 * 
	 * @param table
	 * @param id
	 * @return
	 */
	public boolean isExistsById(String table, String id) {
		return isExistsByField(table, mPrimaryKey, id);
	}

	/**
	 * ���ĳ�ֶ�/ֵ�鿴ĳ������Ƿ����
	 * @return
	 */
	public boolean isExistsByField(String table, String column, String value) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
				.append(column).append(" =?");

		return isExistsBySQL(sql.toString(), new String[] { value });
	}

	/**
	 * ʹ��SQL���鿴ĳ������Ƿ����
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public boolean isExistsBySQL(String sql, String[] selectionArgs) {
		boolean result = false;

		final Cursor c = getDb(false).rawQuery(sql, selectionArgs);
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
	
	public <T> List<T> queryForListBySql(String sql  , RowMapper<T> rowMapper, String[] selectionArgs) {
		List<T> list = new ArrayList<T>();

		final Cursor c = getDb(false).rawQuery(sql, selectionArgs);
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
	 * Query for cursor
	 * 
	 * @param <T>
	 * @param rowMapper
	 * @return a cursor
	 * 
	 * @see SQLiteDatabase#query(String, String[], String, String[], String,
	 *      String, String, String)
	 */
	public <T> T queryForObject(RowMapper<T> rowMapper, String table,
                                String[] columns, String selection, String[] selectionArgs,
                                String groupBy, String having, String orderBy, String limit) {
		T object = null;

		final Cursor c = getDb(false).query(table, columns, selection,
				selectionArgs, groupBy, having, orderBy, limit);
		try {
			if (c.moveToFirst()) {
				object = rowMapper.mapRow(c, c.getCount());
			}
		} finally {
			c.close();
		}
		return object;
	}

	/**
	 * Query for list
	 * 
	 * @param <T>
	 * @param rowMapper
	 * @return list of object
	 * 
	 * @see SQLiteDatabase#query(String, String[], String, String[], String,
	 *      String, String, String)
	 */
	public <T> List<T> queryForList(RowMapper<T> rowMapper, String table,
                                    String[] columns, String selection, String[] selectionArgs,
                                    String groupBy, String having, String orderBy, String limit) {
		List<T> list = new ArrayList<T>();

		final Cursor c = getDb(false).query(table, columns, selection,
				selectionArgs, groupBy, having, orderBy, limit);
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
	 * Get Primary Key
	 * 
	 * @return
	 */
	public String getPrimaryKey() {
		return mPrimaryKey;
	}

	/**
	 * Set Primary Key
	 * 
	 * @param primaryKey
	 */
	public void setPrimaryKey(String primaryKey) {
		this.mPrimaryKey = primaryKey;
	}

	/**
	 * Get Database Connection
	 * 
	 * @param writeable
	 * @return
	 * @see SQLiteOpenHelper#getWritableDatabase();
	 * @see SQLiteOpenHelper#getReadableDatabase();
	 */
	public SQLiteDatabase getDb(boolean writeable) {
		if (writeable) {
			return mDatabaseOpenHelper.getWritableDatabase();
		} else {
			return mDatabaseOpenHelper.getReadableDatabase();
		}
	}

	/**
	 * Some as Spring JDBC RowMapper
	 */
	public interface RowMapper<T> {
		public T mapRow(Cursor cursor, int rowNum);
	}

}
