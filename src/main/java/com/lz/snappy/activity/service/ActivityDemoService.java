package com.lz.snappy.activity.service;

import javax.servlet.http.HttpServletResponse;

import com.lz.snappy.activity.model.RequestMessage;

public interface ActivityDemoService {
	
	public void demo(RequestMessage requestMessage);
	
	public String demo2();
	
	public String queryProImd(String processInstanceId,HttpServletResponse reponse)throws Exception ;
	
	public String queryProHighLighted(String processInstanceId,HttpServletResponse reponse)throws Exception ;
	
	public void myQueryProHighLight(String processInstanceId);
	
	public String rollback(String taskId,String toNodeName,String runProInsId);
}
