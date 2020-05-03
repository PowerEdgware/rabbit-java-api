package com.api.confirm;


import com.api.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ConfirmProducer {

	static String QUEUE_NAME="confirm_queue";
	public static void main(String[] args) {

		Connection conn=null;
		Channel channel=null;
		try {
			conn=ConnectionUtils.getConnection();
			 channel=conn.createChannel();
			channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			
			//设置信道为确认模式
			channel.confirmSelect();//开启确认模式
			
			//发送消息
			channel.basicPublish("", QUEUE_NAME, null, "confirm_msg".getBytes());
			
			//等待消息确认
			boolean ok=channel.waitForConfirms(10*1000);
			System.out.println(ok);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			ConnectionUtils.closeConnection();
		}
	
	}
}
