package com.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MQProducer {

	public static String QUEUE_NAME = "QUEUE_TEST";

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
			// 声明交换机
			// chn.exchangeDeclare(exchange, type)
			// 声明队列信息
			chn.queueDeclare(QUEUE_NAME, false, false, false, null);
			int i = 0;
			for (;;) {
				// 发送消息（没指定交换机则发送到默认直连交换机AMQP Default，Direct）
				chn.basicPublish("", QUEUE_NAME, null, ("hello world" + (i++)).getBytes());
				if (i > 100) {
					break;
				}
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(300));
			}
			chn.close();
			conn.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}

	}
}
