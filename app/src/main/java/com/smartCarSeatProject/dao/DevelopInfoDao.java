package com.smartCarSeatProject.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.smartCarSeatProject.dao.SQLiteTemplate.RowMapper;
import java.util.ArrayList;

public class DevelopInfoDao {

	static SQLiteDatabase mDB;
	DBBaseDao mBaseDao;
	
	public DevelopInfoDao(Context context) {
		String filePath = context.getFilesDir().getAbsolutePath() +"/smart_seat_develop_data.db";
		mDB = SQLiteDatabase.openOrCreateDatabase(filePath,null);
		if (mDB != null){ 
			this.mBaseDao = new DBBaseDao(mDB);
		}
		if (!mBaseDao.tabIsExist(DBContent.DeviceInfo.TABLE_NAME)) {
			mDB.execSQL(DBContent.DeviceInfo.getCreateSQL());
		}
	}
	
	/*****
	 * 添加数据到数据库
	 * @param data
	 * @return
	 */
		public int insertSingleData(DevelopDataInfo data) {
			int result = 0;
			try {
				mDB.insert(DBContent.DeviceInfo.TABLE_NAME,null,makeValues(data));
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
	public ArrayList<DevelopDataInfo> queryHistDataInf(){
			ArrayList<DevelopDataInfo> result = new ArrayList<DevelopDataInfo>();
			try {
				result = mBaseDao.queryForListBySql("select *from "+DBContent.DeviceInfo.TABLE_NAME + " order by saveTime desc ",mRowMapper_MessageData,null );
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

    /*****
     * 根据类型查询所有数据
     *
     * @return
     */
    public ArrayList<DevelopDataInfo> queryHistDataInfByDataType(String strDataType){
        ArrayList<DevelopDataInfo> result = new ArrayList<DevelopDataInfo>();
        try {
            result = mBaseDao.queryForListBySql("select *from "+DBContent.DeviceInfo.TABLE_NAME + " where "+DBContent.DeviceInfo.Columns.dataType+" = '"+strDataType+"' order by saveTime desc ",mRowMapper_MessageData,null );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据名字更新数据
     * @param developDataInfo
     */
    public boolean updateDataByName(DevelopDataInfo developDataInfo) {
        String whereClause = DBContent.DeviceInfo.Columns.dataName+" = ?";
        int i;
        try {
            i = mDB.update(DBContent.DeviceInfo.TABLE_NAME, makeValues(developDataInfo), whereClause, new String[]{developDataInfo.getStrName()});
            return (i== 1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 判断该名称是否已经存在
     * @param strName
     * @return
     */
    public boolean isHaveByName(String strName) {
        ArrayList<DevelopDataInfo> result = new ArrayList<DevelopDataInfo>();
        try {
            result = mBaseDao.queryForListBySql("select *from "+DBContent.DeviceInfo.TABLE_NAME + " where "+DBContent.DeviceInfo.Columns.dataName+" = '"+strName+"'",mRowMapper_MessageData,null );
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.size() > 0) {
            return true;
        }
        return false;

    }

	/**
	 * 删除某个数据
	 * @param developDataInfo
	 * @return
	 */
	public boolean deleteDataByInfo(DevelopDataInfo developDataInfo) {
		String whereClause = DBContent.DeviceInfo.Columns.id+" = ?";
		int i;
		try {
			i = mDB.delete(DBContent.DeviceInfo.TABLE_NAME,  whereClause, new String[]{developDataInfo.getIID()+""});
			return (i== 1);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static final RowMapper<DevelopDataInfo> mRowMapper_MessageData = new RowMapper<DevelopDataInfo>() {
		public DevelopDataInfo mapRow(Cursor cursor, int rowNum) {
			DevelopDataInfo command = new DevelopDataInfo();

			command.setIID(cursor.getInt(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.id)));
			// 名称
			command.setStrName(cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.dataName)));

			// 初始化A面8组气压
			command.setP_init_back_A(cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_init_back_A)));
			// 初始化靠背B面5组气压
			command.setP_init_back_B(cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_init_back_B)));
			// 初始化坐垫3组气压
			String p_init_cushion = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_init_cushion));
			command.setP_init_cushion(p_init_cushion);
			// 识别后靠背A面8组
			String p_recog_back_A = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_A));
			String p_recog_back_B = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_B));
			String p_recog_back_C = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_C));
			String p_recog_back_D = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_D));
			String p_recog_back_E = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_E));
			String p_recog_back_F = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_F));
			String p_recog_back_G = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_G));
			String p_recog_back_H = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_H));

			command.setP_recog_back_A(p_recog_back_A);
			command.setP_recog_back_B(p_recog_back_B);
			command.setP_recog_back_C(p_recog_back_C);
			command.setP_recog_back_D(p_recog_back_D);
			command.setP_recog_back_E(p_recog_back_E);
			command.setP_recog_back_F(p_recog_back_F);
			command.setP_recog_back_G(p_recog_back_G);
			command.setP_recog_back_H(p_recog_back_H);

			// 识别后坐垫3组
			String p_recog_cushion_6= cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_cushion_6));
			String p_recog_cushion_7 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_cushion_7));
			String p_recog_cushion_8 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_cushion_8));

			command.setP_recog_cushion_6(p_recog_cushion_6);
			command.setP_recog_cushion_7(p_recog_cushion_7);
			command.setP_recog_cushion_8(p_recog_cushion_8);

			// 识别后靠背B面5组
			String p_recog_back_1 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_1));
			String p_recog_back_2 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_2));
			String p_recog_back_3 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_3));
			String p_recog_back_4 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_4));
			String p_recog_back_5 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_recog_back_5));

			command.setP_recog_back_1(p_recog_back_1);
			command.setP_recog_back_2(p_recog_back_2);
			command.setP_recog_back_3(p_recog_back_3);
			command.setP_recog_back_4(p_recog_back_4);
			command.setP_recog_back_5(p_recog_back_5);

			// 调节后坐垫3组
			String p_adjust_cushion_6 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_6));
			String p_adjust_cushion_7 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_7));
			String p_adjust_cushion_8 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_8));
			command.setP_adjust_cushion_6(p_adjust_cushion_6);
			command.setP_adjust_cushion_7(p_adjust_cushion_7);
			command.setP_adjust_cushion_8(p_adjust_cushion_8);


			// 调节后靠背B面5组
			String p_adjust_cushion_1 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_1));
			String p_adjust_cushion_2 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_2));
			String p_adjust_cushion_3 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_3));
			String p_adjust_cushion_4 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_4));
			String p_adjust_cushion_5 = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.p_adjust_cushion_5));

			command.setP_adjust_cushion_1(p_adjust_cushion_1);
			command.setP_adjust_cushion_2(p_adjust_cushion_2);
			command.setP_adjust_cushion_3(p_adjust_cushion_3);
			command.setP_adjust_cushion_4(p_adjust_cushion_4);
			command.setP_adjust_cushion_5(p_adjust_cushion_5);

			// 人员-性别
			String m_gender =  cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.m_gender));
			// 人员-国别
			String m_national = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.m_national));
			// 人员-体重
			String m_weight = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.m_weight));
			// 人员-身高
			String m_height = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.m_height));
			// 备注
			String strPSInfo= cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.strPSInfo));
			// 时间
			String saveTime = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.saveTime));
            // 数据类型
            String dataType = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.dataType));
            // 位置调节
            String locationCtr = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.loactionCtr));
            // 心率
            String HeartRate = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.HeartRate));
            // 呼吸率
            String BreathRate = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.BreathRate));
            // 情绪值
            String E_Index = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.E_Index));
            // 舒张压
            String Dia_BP = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.Dia_BP));
            // 收缩压
            String Sys_BP = cursor.getString(cursor.getColumnIndex(DBContent.DeviceInfo.Columns.Sys_BP));

			command.setM_gender(m_gender);
			command.setM_national(m_national);
			command.setM_weight(m_weight);
			command.setM_height(m_height);
			command.setStrPSInfo(strPSInfo);
			command.setSaveTime(saveTime);
			command.setDataType(dataType);
			command.l_location = locationCtr;
			command.setHeartRate(HeartRate);
			command.setBreathRate(BreathRate);
			command.setE_Index(E_Index);
			command.setDia_BP(Dia_BP);
			command.setSys_BP(Sys_BP);

			return command;
		}
	};




		private ContentValues makeValues(DevelopDataInfo temp) {
			ContentValues initialValues = new ContentValues();
			// 遍历所有属性，入库！！！
			initialValues.put(DBContent.DeviceInfo.Columns.dataName, temp.getStrName());
			initialValues.put(DBContent.DeviceInfo.Columns.p_init_back_A, temp.getP_init_back_A());
			initialValues.put(DBContent.DeviceInfo.Columns.p_init_back_B, temp.getP_init_back_B());
			initialValues.put(DBContent.DeviceInfo.Columns.p_init_cushion, temp.getP_init_cushion());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_A, temp.getP_recog_back_A());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_B, temp.getP_recog_back_B());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_C, temp.getP_recog_back_C());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_D, temp.getP_recog_back_D());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_E, temp.getP_recog_back_E());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_F, temp.getP_recog_back_F());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_G, temp.getP_recog_back_G());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_H, temp.getP_recog_back_H());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_cushion_6, temp.getP_recog_cushion_6());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_cushion_7, temp.getP_recog_cushion_7());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_cushion_8, temp.getP_recog_cushion_8());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_1, temp.getP_recog_back_1());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_2, temp.getP_recog_back_2());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_3, temp.getP_recog_back_3());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_4, temp.getP_recog_back_4());
			initialValues.put(DBContent.DeviceInfo.Columns.p_recog_back_5, temp.getP_recog_back_5());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_1, temp.getP_adjust_cushion_1());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_2, temp.getP_adjust_cushion_2());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_3, temp.getP_adjust_cushion_3());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_4, temp.getP_adjust_cushion_4());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_5, temp.getP_adjust_cushion_5());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_6, temp.getP_adjust_cushion_6());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_7, temp.getP_adjust_cushion_7());
			initialValues.put(DBContent.DeviceInfo.Columns.p_adjust_cushion_8, temp.getP_adjust_cushion_8());
			initialValues.put(DBContent.DeviceInfo.Columns.m_gender, temp.getM_gender());
			initialValues.put(DBContent.DeviceInfo.Columns.m_national, temp.getM_national());
			initialValues.put(DBContent.DeviceInfo.Columns.m_weight, temp.getM_weight());
			initialValues.put(DBContent.DeviceInfo.Columns.m_height, temp.getM_height());
			initialValues.put(DBContent.DeviceInfo.Columns.strPSInfo, temp.getStrPSInfo());
			initialValues.put(DBContent.DeviceInfo.Columns.saveTime, temp.getSaveTime());
			initialValues.put(DBContent.DeviceInfo.Columns.dataType, temp.getDataType());
			initialValues.put(DBContent.DeviceInfo.Columns.loactionCtr, temp.l_location);
			initialValues.put(DBContent.DeviceInfo.Columns.HeartRate, temp.getHeartRate());
			initialValues.put(DBContent.DeviceInfo.Columns.BreathRate, temp.getBreathRate());
			initialValues.put(DBContent.DeviceInfo.Columns.Dia_BP, temp.getDia_BP());
			initialValues.put(DBContent.DeviceInfo.Columns.Sys_BP, temp.getSys_BP());
			initialValues.put(DBContent.DeviceInfo.Columns.E_Index, temp.getE_Index());

			return initialValues;
		}
		
		public void closeDb() {
			mDB.close();
		}
		

	
}