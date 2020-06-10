package com.smartCarSeatProject.dao;

public class DBContent {

	/**
	 * �豸�б�
	 * @author Administrator
	 *
	 */
	public static class DeviceInfo {
		
		public static final String TABLE_NAME_Develop = "DevelopDataInfo";
		public static final String TABLE_NAME_Manual = "ManualDataInfo";

		public static class Columns {
			public static final String id = "ID";
			public static final String dataName = "dataName";
			public static final String p_init_back_A = "p_init_back_A";
			public static final String p_init_back_B = "p_init_back_B";
			public static final String p_init_cushion = "p_init_cushion";
			public static final String p_init_cushion_A1 = "p_init_cushion_A1";
			public static final String p_init_cushion_A2 = "p_init_cushion_A2";
			public static final String p_init_cushion_B1 = "p_init_cushion_B1";
			public static final String p_init_cushion_B2 = "p_init_cushion_B2";
			public static final String p_init_cushion_C1 = "p_init_cushion_C1";
			public static final String p_init_cushion_C2 = "p_init_cushion_C2";

			// 识别后靠背A面8组
			public static final String p_recog_back_A = "p_recog_back_A";
			public static final String p_recog_back_B = "p_recog_back_B";
			public static final String p_recog_back_C = "p_recog_back_C";
			public static final String p_recog_back_D = "p_recog_back_D";
			public static final String p_recog_back_E = "p_recog_back_E";
			public static final String p_recog_back_F = "p_recog_back_F";
			public static final String p_recog_back_G = "p_recog_back_G";
			public static final String p_recog_back_H = "p_recog_back_H";

			// 识别后坐垫3组
			public static final String p_recog_cushion_6 = "p_recog_cushion_6";
			public static final String p_recog_cushion_7 = "p_recog_cushion_7";
			public static final String p_recog_cushion_8 = "p_recog_cushion_8";

            public static final String p_recog_cushion_A1 = "p_recog_cushion_A1";
            public static final String p_recog_cushion_A2 = "p_recog_cushion_A2";
            public static final String p_recog_cushion_B1 = "p_recog_cushion_B1";
            public static final String p_recog_cushion_B2 = "p_recog_cushion_B2";
            public static final String p_recog_cushion_C1 = "p_recog_cushion_C1";
            public static final String p_recog_cushion_C2 = "p_recog_cushion_C2";

			// 识别后靠背B面5组
			public static final String p_recog_back_1 = "p_recog_back_1";
			public static final String p_recog_back_2 = "p_recog_back_2";
			public static final String p_recog_back_3 = "p_recog_back_3";
			public static final String p_recog_back_4 = "p_recog_back_4";
			public static final String p_recog_back_5 = "p_recog_back_5";

			// 调节后坐垫3组
			public static final String p_adjust_cushion_6 = "p_adjust_cushion_6";
			public static final String p_adjust_cushion_7 = "p_adjust_cushion_7";
			public static final String p_adjust_cushion_8 = "p_adjust_cushion_8";

			// 调节后靠背B面5组
			public static final String p_adjust_cushion_1 = "p_adjust_cushion_1";
			public static final String p_adjust_cushion_2 = "p_adjust_cushion_2";
			public static final String p_adjust_cushion_3 = "p_adjust_cushion_3";
			public static final String p_adjust_cushion_4 = "p_adjust_cushion_4";
			public static final String p_adjust_cushion_5 = "p_adjust_cushion_5";

			// 人员-性别
			public static final String m_gender = "m_gender";
			// 人员-国别
			public static final String m_national = "m_national";
			// 人员-体重
			public static final String m_weight = "m_weight";
			// 人员-身高
			public static final String m_height = "m_height";
			// 备注
			public static final String strPSInfo = "strPSInfo";
			// 时间
			public static final String saveTime = "saveTime";
            // 类型
            public static final String dataType = "dataType";
            // 位置调节
            public static final String loactionCtr = "loactionCtr";
            // 心率
            public static final String HeartRate = "HeartRate";
            // 呼吸率
            public static final String BreathRate = "BreathRate";
            // 情绪值
            public static final String E_Index = "E_Index";
            // 舒张压
            public static final String Dia_BP = "Dia_BP";
            // 收缩压
            public static final String Sys_BP = "Sys_BP";
            // 信噪比
            public static final String Snr = "Snr";

		}


		public static String getCreateSQLByManual() {
			return "CREATE TABLE " + TABLE_NAME_Manual + "(" + //
					"'"+ Columns.id+"' INTEGER PRIMARY KEY AUTOINCREMENT ," +
	                "'"+ Columns.dataName+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_back_A+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_back_B+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion_A1+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion_A2+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion_B1+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion_B2+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion_C1+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_init_cushion_C2+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_A+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_B+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_C+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_D+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_E+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_F+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_G+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_H+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_1+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_2+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_3+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_4+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_recog_back_5+"' TEXT NOT NULL ," +

                    "'"+ Columns.p_recog_cushion_6+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_7+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_8+"' TEXT NOT NULL ," +

                    "'"+ Columns.p_recog_cushion_A1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_A2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_B1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_B2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_C1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_C2+"' TEXT NOT NULL ," +

	                "'"+ Columns.p_adjust_cushion_1+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_2+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_3+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_4+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_5+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_6+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_7+"' TEXT NOT NULL ," +
	                "'"+ Columns.p_adjust_cushion_8+"' TEXT NOT NULL ," +
	                "'"+ Columns.m_gender+"' TEXT NOT NULL ," +
	                "'"+ Columns.m_national+"' TEXT NOT NULL ," +
	                "'"+ Columns.m_weight+"' TEXT NOT NULL ," +
	                "'"+ Columns.m_height+"' TEXT NOT NULL ," +
	                "'"+ Columns.strPSInfo+"' TEXT NOT NULL ," +
	                "'"+ Columns.saveTime+"' TEXT NOT NULL," +
                    "'"+ Columns.dataType+"' TEXT NOT NULL," +
                    "'"+ Columns.loactionCtr+"' TEXT NOT NULL," +
                    "'"+ Columns.HeartRate+"' TEXT NOT NULL," +
                    "'"+ Columns.BreathRate+"' TEXT NOT NULL," +
                    "'"+ Columns.E_Index+"' TEXT NOT NULL," +
                    "'"+ Columns.Dia_BP+"' TEXT NOT NULL," +
                    "'"+ Columns.Snr+"' TEXT NOT NULL," +
                    "'"+ Columns.Sys_BP+"' TEXT NOT NULL" +
	                ")";
		}

        public static String getCreateSQLByDevelop() {
            return "CREATE TABLE " + TABLE_NAME_Develop + "(" + //
                    "'"+ Columns.id+"' INTEGER PRIMARY KEY AUTOINCREMENT ," +
                    "'"+ Columns.dataName+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_back_A+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_back_B+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion_A1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion_A2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion_B1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion_B2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion_C1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_init_cushion_C2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_A+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_B+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_C+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_D+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_E+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_F+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_G+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_H+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_3+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_4+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_back_5+"' TEXT NOT NULL ," +

                    "'"+ Columns.p_recog_cushion_6+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_7+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_8+"' TEXT NOT NULL ," +

                    "'"+ Columns.p_recog_cushion_A1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_A2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_B1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_B2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_C1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_recog_cushion_C2+"' TEXT NOT NULL ," +

                    "'"+ Columns.p_adjust_cushion_1+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_2+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_3+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_4+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_5+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_6+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_7+"' TEXT NOT NULL ," +
                    "'"+ Columns.p_adjust_cushion_8+"' TEXT NOT NULL ," +
                    "'"+ Columns.m_gender+"' TEXT NOT NULL ," +
                    "'"+ Columns.m_national+"' TEXT NOT NULL ," +
                    "'"+ Columns.m_weight+"' TEXT NOT NULL ," +
                    "'"+ Columns.m_height+"' TEXT NOT NULL ," +
                    "'"+ Columns.strPSInfo+"' TEXT NOT NULL ," +
                    "'"+ Columns.saveTime+"' TEXT NOT NULL," +
                    "'"+ Columns.dataType+"' TEXT NOT NULL," +
                    "'"+ Columns.loactionCtr+"' TEXT NOT NULL," +
                    "'"+ Columns.HeartRate+"' TEXT NOT NULL," +
                    "'"+ Columns.BreathRate+"' TEXT NOT NULL," +
                    "'"+ Columns.E_Index+"' TEXT NOT NULL," +
                    "'"+ Columns.Dia_BP+"' TEXT NOT NULL," +
                    "'"+ Columns.Snr+"' TEXT NOT NULL," +
                    "'"+ Columns.Sys_BP+"' TEXT NOT NULL" +
                    ")";
        }

	}
}
