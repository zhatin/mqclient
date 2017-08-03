package com.bdreport.mq.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bdreport.mq.jdbc.DataRecord;

@Repository
public class JpaRealDataRepository {

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = Logger.getLogger(JpaRealDataRepository.class);

	@Transactional(readOnly = false)
	public void append(JmsRealDataModel dataModel) {
		String sql = "";

		sql = sql
				+ "insert into data_from_mq (gateway_no,data_type,data_date_time,remote_ip,remote_port,data_length,data_list) values (";
		sql = sql + Integer.valueOf(dataModel.getGatewayNo()).toString() + ", ";
		sql = sql + "'" + dataModel.getFuncCode() + "',";
		sql = sql + "'" + dataModel.getDataTime() + "',";
		sql = sql + "'" + dataModel.getIpAddr() + "',";
		sql = sql + Integer.valueOf(dataModel.getInetPort()).toString() + ", ";
		sql = sql + Integer.valueOf(dataModel.getDataLength()).toString() + ", ";
		sql = sql + "'" + dataModel.getDataList().toString() + "'";
		sql = sql + " );";

		logger.debug("SQL: " + sql);

		this.entityManager.createNativeQuery(sql).executeUpdate();
	}

	@Transactional(readOnly = false)
	public void appendList(JmsRealDataModel dataModel) {

		int gateway_no = dataModel.getGatewayNo();
		String data_type = dataModel.getFuncCode();
		String data_date_time = dataModel.getDataTime();
		// int dl = dataModel.getDataLength();
		Map<Integer, List<Float>> dat = dataModel.getDataList();
		Iterator<Map.Entry<Integer, List<Float>>> entries = dat.entrySet().iterator();

		while (entries.hasNext()) {
			Map.Entry<Integer, List<Float>> entry = entries.next();
			int terminator_no = entry.getKey().intValue();
			List<Float> datList = entry.getValue();
			for (int i = 0; i < datList.size(); i++) {
				DataRecord dc = new DataRecord();
				dc.setGateway_no(gateway_no);
				dc.setData_type(data_type);
				dc.setData_date_time(data_date_time);
				dc.setTerminator_no(terminator_no);
				dc.setSensor_no(i + 1);
				dc.setSensor_data(datList.get(i).floatValue());

				this.entityManager.persist(dc);
			}
		}

	}
}
