package com.lz.snappy_activity;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lz.snappy.App;
import com.lz.snappy.activity.service.ActivityDemoService;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class AppTest extends TestCase {

	@Autowired
	private ActivityDemoService activityDemoService;

	//activiti中最核心的类，用它可以产生RepositoryService、RuntimeService、TaskService
	/**
	 * RepositoryService：仓库服务类，所谓的仓库是指流程定义的两个文档：bpmn文件和图片，可以用来删除流程定义
	 * RuntimeService:流程执行服务类，可以从这个service中获取许多关于流程执行的信息；执行管理，包括启动、推进，删除流程实例等操作
	 * TaskService：任务管理，也是任务服务类，可以得到许多相关的任务信息，如当前正在执行的个人待办和用户组待办
	 * 
	 */
	@Autowired
	private ProcessEngine processEngine;

	@Test
	public void testApp() {
		long start = System.currentTimeMillis();
		// for(int i = 0;i < 10000;i ++) {
		// activityDemoService.demo();
		// }
		long end = System.currentTimeMillis();
		// 执行10次所需要的时间：14361
		// 执行1000次所需要的时间：257566
		// 执行10000次所需要的时间：2927388
		System.out.println("执行10000次所需要的时间：" + (end - start));
	}

	/**
	 * 将流程图部署到activiti的数据库表中
	 * 启动完成后在act-re-deployment流程部署表中和act-re-procdef流程定义表中会相应的数据信息
	 */
	@Test
	public void fun() {
		Deployment deploy = processEngine.getRepositoryService().createDeployment()
				.addClasspathResource("processes/MyProcess.bpmn").deploy();
		
		System.out.println("id是："+deploy.getId());
		System.out.println("name是："+deploy.getName());
	}
	
	/**
	 * 启动流程实例
	 * 
	 * 启动流程引擎的时候引擎会先从Authentication读取已认证的用户信息，因此只要能够设置id就可以解决问题
	 * 查看API发现接口IdentityService有一个方法:setAuthenticatedUserId(String authenticatedUserId),
	 * 正是这个方法在其接口实现类:org.activiti.engine.impl.IdentityServiceImpl#setAuthenticatedUserId
	 * 中调用了Authentication.setAuthenticatedUserId()。
	 * 
	 * 为此在启动流程前使用identityService中的方法设置id就可以了
	 */
	@Test
	public void flowStart() {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId("10010");
		//根据流程执行服务类得到一个流程实例类
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("myProcess");
		System.out.println("------------------"+instance.getId());
		System.out.println("------------------"+instance.getDeploymentId());
		
//		RepositoryService repositoryService = processEngine.getRepositoryService();
		//根据流程实例的Id得到一个流程定义
//		Deployment deploy = repositoryService.createDeployment()
//				.addClasspathResource("processes/MyProcess.bpmn").deploy();
//		System.out.println(deploy.getId());//流程id
//		System.out.println(deploy.getName());//流程名
	}
	
	/**
	 * 完成当前任务，将任务推送至下一个任务前
	 * 其中taskId对应act-ru-task表的主键，
	 */
	@Test
	public void completeTask() {
		TaskService taskService = processEngine.getTaskService();
		taskService.complete("180002");
//		taskService.complete("167505");
		System.out.println("完成任务，任务id：7504");
	}
	
	/**
	 * 查询单条任务，根据taskId
	 */
	@Test
	public void completeTask2() {
		Task task = processEngine.getTaskService().createTaskQuery().taskId("80004").singleResult();
		System.out.println(task.getId());
	}
	
	/**
	 * 查询流程定义
	 */
	@Test
	public void findProcessDefinition() {
		List<ProcessDefinition> definitions = processEngine.getRepositoryService().createProcessDefinitionQuery()
		/**指定查询条件
		 * .deploymentId(deploymentId)//根据流程部署对象id查询
		 * .processDefinitionKey(key)//使用流程定义key查询
		 * .processDefinitionKeyLike(keyLike)//使用流程定义的名称模糊查询
		 * .orderByProcessDefinitionVersion().asc()//根据流程版本的升序排列
		 * .orderByProcessDefinitionName().desc()//根据流程定义名称降序排列
		 */
		/**
		 * 返回结果
		 * .singleResult()//返回一个唯一结果
		 * .count()//返回结果集数量
		 * .listPage(firstResult, maxResults)//分页查询
		 */
		.list();//返回结果集，封装流程定义
		
		if(definitions != null && definitions.size() > 0) {
			for(ProcessDefinition pd : definitions) {
				System.out.println("流程定义的id："+pd.getId());
				System.out.println("流程定义的name："+pd.getName());
				System.out.println("流程定义的key："+pd.getKey());
				System.out.println("流程定义的版本："+pd.getVersion());
				System.out.println("流程定义的bpmn文件："+pd.getResourceName());
				System.out.println("流程定义的png图片："+pd.getDiagramResourceName());
				System.out.println("部署id："+pd.getDeploymentId());
				System.out.println("############################################333");
			}
		}
	}
	
	/**
	 * 删除流程定义
	 */
	@Test
	public void deleteDefinition() {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		repositoryService.deleteDeployment("77501", true);
	}
	
	/**
	 * 自己写的查询流程图
	 */
	@Test
	public void queryProcssImg() {
		activityDemoService.myQueryProHighLight("202501");
	}
}
