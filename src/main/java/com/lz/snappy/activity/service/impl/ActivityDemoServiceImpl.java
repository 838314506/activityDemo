package com.lz.snappy.activity.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Message;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lz.snappy.activity.model.RequestMessage;
import com.lz.snappy.activity.service.ActivityDemoService;

@Service
public class ActivityDemoServiceImpl implements ActivityDemoService {

	@Autowired
	private RepositoryService reposityService;

	@Autowired
	private RuntimeService runService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private ProcessEngineConfigurationImpl processEngineConfig;

	@Override
	public void demo(RequestMessage requestMessage) {
		String taskId = "197502";
		System.out.println(requestMessage);
		Map<String, Object> map = new HashMap<>();
		if (requestMessage != null) {
			map.put("count", requestMessage.getCount());
			map.put("name", requestMessage.getName());
			map.put("country", requestMessage.getCountry());
		}
		System.out.println(map);
		taskService.complete(taskId, map);
	}

	@Override
	public String demo2() {
		return null;
	}

	/**
	 * 生成流程图
	 * 
	 * @throws Exception
	 */
	@Override
	public String queryProImd(String processInstanceId, HttpServletResponse response)
			throws Exception {
		ProcessInstance instance = runService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		BpmnModel bpmnModel = reposityService.getBpmnModel(instance.getProcessDefinitionId());
		ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();
		List<String> high = new ArrayList<>();
		InputStream is = diagramGenerator.generateDiagram(bpmnModel,"png",high,high,"宋体","宋体","宋体",null,1.0);
		int size = is.available();
		byte[] bytes = new byte[size];
		is.read(bytes);
		//用于浏览器显示类型，否则浏览器中不显示
		response.setContentType("image/png");
		OutputStream os = response.getOutputStream();
		os.write(bytes);
		os.flush();
		os.close();
		is.close();
		
		return "操作成功，图片保存完成！";
	}

	/**
	 * 生成流程图的同时高亮显示
	 */
	@Override
	public String queryProHighLighted(String processInstanceId,HttpServletResponse response) throws Exception {
		ProcessInstance instance = runService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		
		List<Execution> executeId = runService.createExecutionQuery().processInstanceId(processInstanceId).list();
		List<String> activitiId = new ArrayList<>();
		List<String> flows = new ArrayList<>();
		for (Execution execute : executeId) {
			List<String> ids = runService.getActiveActivityIds(execute.getId());
			activitiId.addAll(ids);
		}
		
		BpmnModel bpmnModel = reposityService.getBpmnModel(instance.getProcessDefinitionId());
		ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();
		InputStream is = diagramGenerator.generateDiagram(bpmnModel,"png",activitiId,flows,"宋体","宋体","宋体",null,1.0);
		int size = is.available();
		byte[] bytes = new byte[size];
		is.read(bytes);
		//用于浏览器显示类型，否则浏览器中不显示
		response.setContentType("image/png");
		OutputStream os = response.getOutputStream();
		os.write(bytes);
		os.flush();
		os.close();
		is.close();

		return "操作成功，图片保存完成！";
	}


	//自己写的读取流程图
	@SuppressWarnings("resource")
	public void myQueryProHighLight(String processInstanceId) {// 202503
		// 使用historyService创建一个历史活动实例查询对象
		HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
				.createHistoricProcessInstanceQuery();
		// 通过使用查询对象中的api来定义一个流程实例id，并指定最终的查询结果是单条还是多条，多条时使用list(),单条使用singleResult()
		HistoricProcessInstance historicProcessInstance = historicProcessInstanceQuery
				.processInstanceId(processInstanceId).singleResult();
		System.out.println("通过id查询到的部署id是：" + historicProcessInstance.getDeploymentId()
				+ "  流程定义id是：" + historicProcessInstance.getProcessDefinitionId());

		// 通过从查询出来的流程实例中获取流程定义id，根据定义id，让reposityService去表中查找对应部署的图片得到一个输出流
		InputStream is = reposityService
				.getProcessDiagram(historicProcessInstance.getProcessDefinitionId());

		try {
			BufferedImage read = ImageIO.read(is);

			File file = new File("C:\\Users\\lzz\\Desktop\\myPhoto.png");
			if (!file.exists())
				file.createNewFile();

			FileOutputStream os = new FileOutputStream(file);
			ImageIO.write(read, "png", os);
			os.close();
			is.close();
			System.out.println("自己写的成功啦！");

		} catch (IOException e) {
			System.out.println("出现了异常，异常信息为：" + e.getMessage());
		}

	}

	/**
	 * 根据一个任务id和一个运行时流程实例id来进行一个流程任务回退的操作
	 */
	@Override
	public String rollback(String taskId, String toNodeName, String runProInsId) {
		// 1、根据任务id来获取上一个任务的id
		TaskEntity presentTask = (TaskEntity) taskService.createTaskQuery().taskId(taskId)
				.singleResult();
		// String preTaskId = presentTask.getParentTaskId();

		// System.out.println("当前taskId的上一级taskId是：" + preTaskId);
		System.out.println("当前taskId是：" + taskId + "  taskId所对应的name：" + presentTask.getName());
		System.out.println("获取当前taskId的executionId:" + presentTask.getExecutionId());
		List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
				.executionId(presentTask.getExecutionId()).list();

		// HistoricActivityInstance previousInstance = null;

		// int gwayIndex = ifContainsGateWay(list);
		List<HistoricActivityInstance> delNode = new ArrayList<>();
		// 获取想要跳转节点以后的所有节点，将它们放入一个集合中
		for (int i = 0; i < list.size(); i++) {
			HistoricActivityInstance instance = list.get(i);
			if (instance.getActivityName().equals(toNodeName)) {
				for (int j = i + 1; j < list.size(); j++) {
					delNode.add(list.get(j));
				}
			}
		}

		// ProcessDefinitionEntity processDefinition =
		// (ProcessDefinitionEntity)reposityService.getProcessDefinition("");
		//
		// ActivityImpl curAct= processDefinition.findActivity("");
		// List<PvmTransition> outTrans= curAct.getOutgoingTransitions();

		for (HistoricActivityInstance e : delNode) {
			System.out.println(e.getTaskId());
			System.out.println(e.getActivityName());
		}

		return "回退任务成功，可进行查看！";
	}

}
