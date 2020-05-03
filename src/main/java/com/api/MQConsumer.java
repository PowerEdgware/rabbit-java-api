package com.api;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class MQConsumer {
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

			// 声明队列信息
			chn.queueDeclare(QUEUE_NAME, false, false, false, null);

			System.out.println("wait for message");

			DefaultConsumer consumer = new DefaultConsumer(chn) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
						byte[] body) throws IOException {
					System.out.println(
							"Received Msg=" + new String(body) + " tag=" + consumerTag + " envelope=" + envelope);
					//do business
					chn.basicAck(envelope.getDeliveryTag(), false);
				}
			};
			chn.basicQos(5);//服务端限流
			chn.basicConsume(QUEUE_NAME, false, consumer);
//			chn.basicReject(deliveryTag, requeue);
//			chn.basicNack(deliveryTag, multiple, requeue);
			

			chn.close();
			conn.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}

	}

}
