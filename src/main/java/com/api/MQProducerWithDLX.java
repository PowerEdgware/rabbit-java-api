package com.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MQProducerWithDLX {

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
			String QUEUE_NAME="MY_DEMO_QUEUE";
			//send msg
			int i = 0;
			for (;;) {
				// 发送消息（没指定交换机则发送到默认直连交换机AMQP Default，Direct）
				AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
						.deliveryMode(2) // 持久化消息
						.contentEncoding("UTF-8")
						.expiration("10000") // TTL
						.build();
				chn.basicPublish("", QUEUE_NAME, props, ("hello world" + (i++)).getBytes());
				if (i > 100) {
					break;
				}
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
			}
			chn.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
