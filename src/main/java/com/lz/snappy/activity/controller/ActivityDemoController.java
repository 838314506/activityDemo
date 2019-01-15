package com.lz.snappy.activity.controller;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lz.snappy.activity.model.RequestMessage;
import com.lz.snappy.activity.service.ActivityDemoService;

@RestController
@RequestMapping("/activity")
public class ActivityDemoController {
	
	@Autowired
	private ActivityDemoService activityDemoService;
	
	@GetMapping("/demo")
	public String demo(RequestMessage requestMessage) {
		activityDemoService.demo(requestMessage);
		return "success";
	}
	@GetMapping("/demo2")
	public String demo2() {
		return	activityDemoService.demo2();
	}
	
	@GetMapping("/queryProImg")
	public String queryProImd(String processInstanceId,HttpServletResponse reponse) {
		String result = "";
		try {
			result = activityDemoService.queryProImd(processInstanceId,reponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@GetMapping("/queryProHighLighted")
	public String queryProHighLighted(String processInstanceId,HttpServletResponse reponse) {
		String result = "";
		try {
			result = activityDemoService.queryProHighLighted(processInstanceId,reponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 用于回退任务，根据当前任务id和一个运行时流程实例的id
	 * @param taskId
	 * @param runProInsId
	 * @return
	 */
	@GetMapping("/rollback")
	public String rollback(String taskId,String toNodeName,String runProInsId) {
		
		return activityDemoService.rollback(taskId, toNodeName,runProInsId);
	}
}
