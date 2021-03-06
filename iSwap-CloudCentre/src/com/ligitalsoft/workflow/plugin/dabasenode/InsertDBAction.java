package com.ligitalsoft.workflow.plugin.dabasenode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.jbpm.api.activity.ActivityExecution;

import com.common.dbtool.DBConntTool;
import com.common.dbtool.FileDBTool;
import com.common.utils.date.DateUtil;
import com.ligitalsoft.help.filedb.FileDBConstant;
import com.ligitalsoft.workflow.exception.ActionException;
import com.ligitalsoft.workflow.plugin.PluginActionHandler;
import com.ligitalsoft.workflow.plugin.model.DataPackInfo;
import com.ligitalsoft.workflow.plugin.model.FiledDataInfo;
import com.ligitalsoft.workflow.plugin.model.RowDataInfo;

/**
 *将缓存的数据取出来写的数据库中
 *@Company 中海纪元
 *@author   hudaowan
 *@version   iSwap V6.0 数据交换平台  
 *@date   2011-9-5 上午11:48:35
 *@Team 研发中心
 */
public class InsertDBAction extends PluginActionHandler {
	private static final long serialVersionUID = 252064412412668172L;
	public String dataSource;//数据源
	public String tableName;//指标项
	public String dataList_inputVar;//将数据写入缓存是的 ，缓存的key
	public String insertData_outVar;//插入数据库的总数
	
	@SuppressWarnings("unchecked")
	@Override
	public void doexecute(ActivityExecution context) throws ActionException {
		log.info("开始将数据入库......");
		if(null!=this.getCacheInfo(dataList_inputVar)&&""!=this.getCacheInfo(dataList_inputVar)){
		
		try{
			List<DataPackInfo> dpInfoList = (List<DataPackInfo>)this.getCacheInfo(dataList_inputVar);
			if(null!=dpInfoList&&dpInfoList.size()>0){
	    		if(dpInfoList.get(0).getRowDataList().size()>0){
					
					
	    			this.cacheToDB(dpInfoList,context);
	    		}else{
	    			log.error("没有数据需要入库！");
	    		}
	    	}else{
	    		log.error("没有数据需要入库！");
	    	}
	    	
		}catch(Exception e){
			 ByteArrayOutputStream bo = new ByteArrayOutputStream();
			 e.printStackTrace(new PrintStream(bo));
			 log.error("插入数据库失败！",e);
			 throw new ActionException(e);
		}
		}else{
			log.info("数据添加节点时未获得【"+dataList_inputVar+"】变量的值！");
		}
	}
	
	/**
	 * 得到数据库的连接
	 * @return
	 * @throws ActionException 
	 * @author  hudaowan
	 * @date 2011-9-5 上午11:08:39
	 */
	public  Connection connDB(DBConntTool dbTool) throws ActionException{
		Connection conn = null;
		FileDBTool tool = FileDBTool.init();
		try{
	    	tool.getMongoConn();
	    	Map<String,Object> find_map = new HashMap<String,Object>();
	    	find_map.put("key", dataSource);
	    	List<Map<String,Object>> entityList = tool.findToFiledb(FileDBConstant.fileDBName, FileDBConstant.dataSourceDB, find_map);
			Map<String,Object> entity = entityList.get(0);
			String driveName = (String)entity.get("driveName");
	    	String address =  (String)entity.get("address");
	    	String userName = (String)entity.get("userName");
	    	String passWord = (String)entity.get("passWord");
	        conn = dbTool.getConn(driveName,address,userName,passWord);
	    	
		}catch(Exception e){
			log.error("连接数据库失败!", e);
			throw new ActionException(e);
		}finally{
			tool.closeFileDB();
		}
		return conn;
	}
	
	/**
	 * 得到缓存中的数据
	 * @return
	 * @throws ActionException 
	 * @author  hudaowan
	 * @date 2011-9-5 下午04:16:22
	 */
	public void cacheToDB(List<DataPackInfo> dpInfoList,ActivityExecution context)  throws Exception{
		String sql = null;
		int n=1;
		DBConntTool dbTool = DBConntTool.bcpoolInit();
		
		for(DataPackInfo dpf:dpInfoList){
			Connection conn = this.connDB(dbTool);
			conn.setAutoCommit(false);
			PreparedStatement pstmt = null;
			try{
			List<RowDataInfo> RowDataList = dpf.getRowDataList();
				if(RowDataList.size()>0){
				for(RowDataInfo rdi:RowDataList){
					if(sql==null){
						 sql = this.getSql(rdi.getFiledDataInfos());
						 log.info("insert:"+sql);
					 }
					if(pstmt==null){
						 pstmt = conn.prepareStatement(sql);
					}
					this.setParameter(pstmt,rdi.getFiledDataInfos());
					pstmt.addBatch();
					log.info("第：【"+n+"】条数据处理完成.");
					n++;
				}
				if(pstmt!=null){
					 pstmt.executeBatch(); 
				 }
				 conn.commit();
				 pstmt.close();
				 dbTool.closeConn(conn);
				 pstmt = null;
					
				}
			}catch(Exception e){
				 ByteArrayOutputStream bo = new ByteArrayOutputStream();
				 e.printStackTrace(new PrintStream(bo));
				 log.error("插入数据库失败！",e);
				 throw new ActionException(e);
			}finally{
				 dbTool.closeConn(conn);
				 dbTool.shutdownConnPool();
			}
		}
		context.setVariable(insertData_outVar, n-1);
	}
	
	 /**
	  * 生成一条sql语句
	  *@author hudaowan
	  *@date  Sep 3, 2008 10:49:09 AM
	  *@param tabInfoList
	  *@return
	  */
	 private String getSql(List<FiledDataInfo> tabInfoList){
		 StringBuffer sbFiled = new StringBuffer();
		 StringBuffer sbwhere= new StringBuffer();
		 for(FiledDataInfo tabInfo:tabInfoList){
			 if(!StringUtils.isBlank(tabInfo.getType())&&!"timestamp".equals(tabInfo.getType().toLowerCase())){
				 sbFiled.append(tabInfo.getFiledName()).append(",");
				 sbwhere.append("?").append(",");
			 }
		 }
		  String sql = "insert into "+this.tableName+"("+sbFiled.toString().substring(0, sbFiled.toString().length()-1)+") values("+
			 sbwhere.toString().substring(0, sbwhere.toString().length()-1)+")";
		 return sql;
	 }
	
	 /**
	  * 拼写值
	  *@author hudaowan
	  *@date  Sep 4, 2008 10:42:21 AM
	  *@param query
	  *@param tabInfoList
	  *@throws ParseException
	  */
	 private void setParameter(PreparedStatement pstmt,List<FiledDataInfo> tabInfoList) throws Exception{
		 int i=1;
		 for(FiledDataInfo tabInfo:tabInfoList){
		     if(!StringUtils.isBlank(tabInfo.getType())&&"timestamp".equals(tabInfo.getType().toLowerCase())){
//		    	 Timestamp sqlTimestamp = new Timestamp(System.currentTimeMillis());
//		    	 pstmt.setTimestamp(i,sqlTimestamp);
		    	 i--;
		     }else if(!StringUtils.isBlank(tabInfo.getType())&&"date".equals(tabInfo.getType().toLowerCase())){
		    	 String dateTime = tabInfo.getFiledValue();
		    	 if(!"".equals(tabInfo.getFiledValue())&&dateTime!=null){
		    			if(tabInfo.getFiledValue().contains("-")&&tabInfo.getFiledValue().length()>18){
							SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
							Date date = sdf.parse(tabInfo.getFiledValue().trim());
							pstmt.setDate(i, new java.sql.Date(date.getTime()));
						}else if(tabInfo.getFiledValue().contains("年")){
							SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy年MM月dd日");
							Date date = sdf.parse(tabInfo.getFiledValue().trim());
							date=DateUtil.strToDate(DateUtil.formatDate(date, "yyyy-MM-dd"), "yyyy-MM-dd");
							pstmt.setDate(i, new java.sql.Date(date.getTime()));
						}else if(tabInfo.getFiledValue().contains("/")){
							SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy/MM/dd");
							Date date = sdf.parse(tabInfo.getFiledValue().trim());
							date=DateUtil.strToDate(DateUtil.formatDate(date, "yyyy-MM-dd"), "yyyy-MM-dd");
							pstmt.setDate(i, new java.sql.Date(date.getTime()));
						}else{
							SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd");
							Date date = sdf.parse(tabInfo.getFiledValue().trim());
							pstmt.setDate(i, new java.sql.Date(date.getTime()));
						}
		    			
		    	 }else{
		    		 if("true".equals(tabInfo.getIsbuild())){
		    			 Date date = new Date();
						 pstmt.setDate(i, new java.sql.Date(date.getTime()));
		    		 }else{
		    			 Date date = new Date();
		    			 pstmt.setDate(i, new java.sql.Date(date.getTime())); 
		    		 }
		    	 }
			 }else if("true".equals(tabInfo.getIsbuild())){
				 String str = System.currentTimeMillis()+"";
				 String id = this.genRandNum(5)+str.substring(str.length()-4,str.length());;
				 if("integer".equals(tabInfo.getType().toLowerCase())){
					 pstmt.setInt(i, Integer.parseInt(id)); 
				 }else{
					 pstmt.setString(i, id); 
				 }
			 }else if(!StringUtils.isBlank(tabInfo.getType())&&"integer".equals(tabInfo.getType().toLowerCase())){
				 if(null!=tabInfo.getFiledValue()&&!"".equals(tabInfo.getFiledValue())){
					 pstmt.setInt(i, Integer.valueOf(tabInfo.getFiledValue())); 
				 }else{
					 pstmt.setBigDecimal(i, null);
				 }
			 }else if(!StringUtils.isBlank(tabInfo.getType())&&"number".equals(tabInfo.getType().toLowerCase())){
				 if(null!=tabInfo.getFiledValue()&&!"".equals(tabInfo.getFiledValue())){
					 pstmt.setFloat(i,Float.valueOf(tabInfo.getFiledValue())); 
				 }else{
					 pstmt.setBigDecimal(i, null);
				 }
			 }
			 else{
				 pstmt.setString(i, tabInfo.getFiledValue());
			 }
		     i++;
		 }
		 
	 }
	 
	public String genRandNum(int pwd_len) {
		// 35是因为数组是从0开始的，26个字母+10个数字
		final int maxNum = 36;
		int i; // 生成的随机数
		int count = 0; // 生成的密码的长度
		char[] str = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while (count < pwd_len) {
			// 生成随机数，取绝对值，防止生成负数，
			i = Math.abs(r.nextInt(maxNum)); // 生成的数最大为36-1
			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count++;
			}
		}
		return pwd.toString();
	}
}
