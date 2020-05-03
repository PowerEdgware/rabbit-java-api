package com.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class PriorityMessageQueue {

	public static void main(String[] args) {
		ConnectionFactory factory = new ConnectionFactory();
		// 连接地址和端口
		factory.setHost(Constants.RABBITMQ_HOST);
		factory.setPort(Constants.RABBITMQ_PORT);
		// 虚拟主机
		factory.setVirtualHost(Constants.RABBITMQ_VIRTUAL_HOST);
		factory.setAutomaticRecoveryEnabled(true);
		// 账户信息
		factory.setUsername(Constants.RABBITMQ_USER);
		factory.setPassword(Constants.RABBITMQ_PASSWD);

		// 建立连接
		Connection conn = null;
		try {
			conn = factory.newConnection();
			// 建立通道
			Channel chn = conn.createChannel();
			// 通过参数指定队列的优先级发送消息时指定消息的优先级x-max-priority
			Map<String, Object> mapArgs = ImmutableMap.of("x-max-priority", 10);
			chn.queueDeclare("my_priority_queue", true, false, false, mapArgs);
			
			//消息堆积时，优先级高的消息可以优先被消费
			AMQP.BasicProperties props=new AMQP.BasicProperties().builder()
					.deliveryMode(2)
					.priority(7)
					.build();
			chn.basicPublish("", "my_priority_queue", props, "prority_msg".getBytes());
			
			chn.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
