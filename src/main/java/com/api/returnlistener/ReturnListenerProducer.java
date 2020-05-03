package com.api.returnlistener;

import com.api.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ReturnListenerProducer {



	static String QUEUE_NAME="returnlistener_queue";
	public static void main(String[] args) {

		Connection conn=null;
		Channel channel=null;
		try {
			conn=ConnectionUtils.getConnection();
			channel=conn.createChannel();
//			 channel.addConfirmListener(listener);
//			channel.addConfirmListener(ackCallback, nackCallback);
			
			//设置投递失败时的监听器(没办法进入队列)
			//TODO 失败原因可能是，队列不存在路由关键字错误等
			channel.addReturnListener(ret->{
				//可以重新发送
				System.out.println("returned="+new String(ret.getBody()));
			});
			//TODO 不创建队列 channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			
			//设置信道为确认模式
			channel.confirmSelect();//开启确认模式
			
			//发送消息
			channel.basicPublish("", QUEUE_NAME, true, null, "return_listener_msg".getBytes());
			
			//等待消息确认
			//TODO 路由到不存在绑定关系的routingKey
			boolean ok=channel.waitForConfirms(10*1000);
			System.out.println(ok);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			ConnectionUtils.closeConnection();
		}
	
	}

}
