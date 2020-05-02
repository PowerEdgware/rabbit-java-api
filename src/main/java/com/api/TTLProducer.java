package com.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class TTLProducer {

	public static String QUEUE_NAME = "TEST_TTL_MESSAGE_QUEUE";

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

			// TODO 通过队列设置消息过期时间
			// Map<String,Object> maps=ImmutableMap.of("x-message-ttl", 10000);
			// 声明队列信息
			// chn.queueDeclare(QUEUE_NAME, false, false, false, maps);
			// TODO 通过消息属性设置消息过期时间
			chn.queueDeclare(QUEUE_NAME, false, false, false, null);
			int i = 0;
			for (;;) {
				// 发送消息（没指定交换机则发送到默认直连交换机AMQP Default，Direct）
				String msg = ("hello world" + (i++));
				// 设置消息ttl属性
				BasicProperties properties = new BasicProperties().builder().deliveryMode(2).expiration("10000")// 过期时间：毫秒
						.build();

				// chn.basicPublish("", QUEUE_NAME, null, msg.getBytes());
				chn.basicPublish("", QUEUE_NAME, properties, msg.getBytes());
				if (i > 10) {
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
