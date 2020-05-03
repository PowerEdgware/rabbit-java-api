package com.api.persistence;

import com.api.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/*
 * 确保消息的持久化，避免服务重启、宕机等因素导致的消息丢失
 */
public class PersistenceMsgProducer {

	public static void main(String[] args) {
		Connection conn = null;
		Channel channel;
		try {
			conn = ConnectionUtils.getConnection();
			channel = conn.createChannel();

			// 声明持久化类型的交换机
			String myPersistenceExchange = "my_persist_exchange";
			channel.exchangeDeclare(myPersistenceExchange, BuiltinExchangeType.TOPIC, true, false, false, null);

			// 声明持久化队列
			String myPersistenceQueue = "my_persist_queue";
			channel.queueDeclareNoWait(myPersistenceQueue, true, false, false, null);

			// 绑定队列和交换机
			String bindingKey = "my_persist.*.key";//带有通配符的bindingKey
			channel.queueBind(myPersistenceQueue, myPersistenceExchange, bindingKey, null);

			// 发送持久化的消息
			String routingKey = "my_persist.test.key";
			AMQP.BasicProperties props = new BasicProperties().builder().deliveryMode(2)// 代表持久化消息
					.build();
			channel.basicPublish(myPersistenceExchange, routingKey, props, "my_perisitence_msg".getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionUtils.closeConnection();
		}
	}
}
