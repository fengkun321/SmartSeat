package com.smartCarSeatProject.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartCarSeatProject.dao.SQLiteTemplate.RowMapper;

import java.util.ArrayList;

public class MemoryInfoDao {

	static SQLiteDatabase mDB;
	DBBaseDao mBaseDao;

	public MemoryInfoDao(Context context) {
		String filePath = context.getFilesDir().getAbsolutePath() +"/smart_seat_develop_data.db";
		mDB = SQLiteDatabase.openOrCreateDatabase(filePath,null);
		if (mDB != null){ 
			this.mBaseDao = new DBBaseDao(mDB);
		}
		if (!mBaseDao.tabIsExist(DBContent.DeviceInfo.TABLE_NAME_MEMORY)) {
			mDB.execSQL(DBContent.DeviceInfo.getCreateMemorySQL());
		}
	}
	
	/*****
	 * 添加数据到数据库
	 * @param data
	 * @return
	 */
		public int insertSingleData(MemoryDataInfo data) {
			int result = 0;
			try {
				mDB.insert(DBContent.DeviceInfo.TABLE_NAME_MEMORY,null,makeValues(data));
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
			return result;
		}



	/*****
	 * 查询所有历史数据
	 *
	 * @return
	 */
	public ArrayList<MemoryDataInfo> queryHistDataInf(){
			ArrayList<MemoryDataInfo> result = new ArrayList<MemoryDataInfo>();
			try {
				result = mBaseDao.queryForListBySql("select *from "+DBContent.DeviceInfo.TABLE_NAME_MEMORY + " order by "+DBContent.DeviceInfo.ColumnsMemory.id+" desc ",mRowMapper_MessageData,null );
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
	}

	/*****
	 * 根据名称查询某个设备是否存在
	 *
	 * @return
	 */
	public boolean isHaveByName(String strName){
		ArrayList<MemoryDataInfo> result = queryHistDataInfByName(strName);
		if (result != null && result.size() > 0)
			return true;
		else
			return false;
	}

	/*****
	 * 根据名称查询某个设备
	 *
	 * @return
	 */
	public ArrayList<MemoryDataInfo> queryHistDataInfByName(String strName){
		ArrayList<MemoryDataInfo> result = new ArrayList<MemoryDataInfo>();
		try {
			result = mBaseDao.queryForListBySql("select *from "+DBContent.DeviceInfo.TABLE_NAME_MEMORY + " where "+DBContent.DeviceInfo.ColumnsMemory.dataName+" = ?" ,mRowMapper_MessageData,new String[]{strName} );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据名称替换某条数据
	 * @param developDataInfo
	 * @return
	 */
	public boolean updateDataByName(MemoryDataInfo developDataInfo) {
		String whereClause = DBContent.DeviceInfo.ColumnsMemory.dataName+" = ?";
		int i;
		try {
			i = mDB.update(DBContent.DeviceInfo.TABLE_NAME_MEMORY, makeValues(developDataInfo), whereClause, new String[]{developDataInfo.getStrDataName()});
			return (i== 1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}


	}

	/**
	 * 删除某个数据
	 * @param developDataInfo
	 * @return
	 */
	public boolean deleteDataByInfo(MemoryDataInfo developDataInfo) {
		String whereClause = DBContent.DeviceInfo.ColumnsMemory.id+" = ?";
		int i;
		try {
			i = mDB.delete(DBContent.DeviceInfo.TABLE_NAME_MEMORY,  whereClause, new String[]{developDataInfo.getIID()+""});
			return (i== 1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static final RowMapper<MemoryDataInfo> mRowMapper_MessageData = new RowMapper<MemoryDataInfo>() {
		public MemoryDataInfo mapRow(Cursor cursor, int rowNum) {
			MemoryDataInfo command = new MemoryDataInfo();

			command.setIID(cursor.getInt(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.id)));
			// 名称
			command.setStrDataName(cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.dataName)));

			String p_recog_back_A = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressA));
			String p_recog_back_B = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressB));
			String p_recog_back_C = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressC));
			String p_recog_back_D = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressD));
			String p_recog_back_E = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressE));
			String p_recog_back_F = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressF));
			String p_recog_back_G = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressG));
			String p_recog_back_H = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPressH));

			command.setStrPressA(p_recog_back_A);
			command.setStrPressB(p_recog_back_B);
			command.setStrPressC(p_recog_back_C);
			command.setStrPressD(p_recog_back_D);
			command.setStrPressE(p_recog_back_E);
			command.setStrPressF(p_recog_back_F);
			command.setStrPressG(p_recog_back_G);
			command.setStrPressH(p_recog_back_H);

			String p_recog_cushion_1 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress1));
			String p_recog_cushion_2 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress2));
			String p_recog_cushion_3 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress3));

			String p_recog_back_4 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress4));
			String p_recog_back_5 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress5));
			String p_recog_back_6 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress6));
			String p_recog_back_7 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress7));
			String p_recog_back_8 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.ColumnsMemory.strPress8));

			command.setStrPress1(p_recog_cushion_1);
			command.setStrPress2(p_recog_cushion_2);
			command.setStrPress3(p_recog_cushion_3);
			command.setStrPress4(p_recog_back_4);
			command.setStrPress5(p_recog_back_5);
			command.setStrPress6(p_recog_back_6);
			command.setStrPress7(p_recog_back_7);
			command.setStrPress8(p_recog_back_8);

			return command;
		}
	};

	private ContentValues makeValues(MemoryDataInfo temp) {
		ContentValues initialValues = new ContentValues();
		// 遍历所有属性，入库！！！
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.dataName, temp.getStrDataName());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressA, temp.getStrPressA());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressB, temp.getStrPressB());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressC, temp.getStrPressC());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressD, temp.getStrPressD());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressE, temp.getStrPressE());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressF, temp.getStrPressF());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressG, temp.getStrPressG());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPressH, temp.getStrPressH());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress1, temp.getStrPress1());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress2, temp.getStrPress2());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress3, temp.getStrPress3());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress4, temp.getStrPress4());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress5, temp.getStrPress5());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress6, temp.getStrPress6());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress7, temp.getStrPress7());
		initialValues.put(DBContent.DeviceInfo.ColumnsMemory.strPress8, temp.getStrPress8());

		return initialValues;
	}

	public void closeDb() {
		mDB.close();
	}
		

	
}