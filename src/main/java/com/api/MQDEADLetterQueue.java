package com.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
/**
 * 消息变成死信的条件：
 *1.The message is negatively acknowledged by a consumer using basic.reject or basic.nack 
 *with requeue parameter set to false.
2.The message expires due to per-message TTL; or
3.The message is dropped because its queue exceeded a length limit
 *
 */
public class MQDEADLetterQueue {

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

			// 指定队列声明时所使用的死信交换机
			// 通过参数指定
			String deadLetterExchange="DLX_EXCHANGE";
			Map<String, Object> mapArgs = ImmutableMap.of("x-dead-letter-exchange", deadLetterExchange,
					"x-dead-letter-routing-key", "DLX_KEY");
			//声明普通队列时指定死信交换机，以便丢失的消息可以被投递到死信交换机
			chn.queueDeclare("MY_DEMO_QUEUE", false, false, false, mapArgs);
			
			//声明死信交换机并绑定死信队列
			chn.exchangeDeclare(deadLetterExchange, "topic", false, false, true, null);
			String dlxQueue="DLX_QUEUE";
			chn.queueDeclare(dlxQueue, false, false, false, null);
			
			//绑定死信队列到死信交换机
			chn.queueBind(dlxQueue, deadLetterExchange, "#");
			
			System.out.println("wait for dead letter");
			DefaultConsumer consumer = new DefaultConsumer(chn) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
						byte[] body) throws IOException {
					System.out.println(
							"Received Msg=" + new String(body) + " tag=" + consumerTag + " envelope=" + envelope);
				}
			};
			
			chn.basicConsume(dlxQueue, true, consumer);
			System.in.read();
			chn.close();
			conn.close();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}

	}
}
