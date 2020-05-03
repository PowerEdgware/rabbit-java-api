package com.api.persistence;

import com.api.ConnectionUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/*
 * ȷ����Ϣ�ĳ־û����������������崻������ص��µ���Ϣ��ʧ
 */
public class PersistenceMsgProducer {

	public static void main(String[] args) {
		Connection conn = null;
		Channel channel;
		try {
			conn = ConnectionUtils.getConnection();
			channel = conn.createChannel();

			// �����־û����͵Ľ�����
			String myPersistenceExchange = "my_persist_exchange";
			channel.exchangeDeclare(myPersistenceExchange, BuiltinExchangeType.TOPIC, true, false, false, null);

			// �����־û�����
			String myPersistenceQueue = "my_persist_queue";
			channel.queueDeclareNoWait(myPersistenceQueue, true, false, false, null);

			// �󶨶��кͽ�����
			String bindingKey = "my_persist.*.key";//����ͨ�����bindingKey
			channel.queueBind(myPersistenceQueue, myPersistenceExchange, bindingKey, null);

			// ���ͳ־û�����Ϣ
			String routingKey = "my_persist.test.key";
			AMQP.BasicProperties props = new BasicProperties().builder().deliveryMode(2)// ����־û���Ϣ
					.build();
			channel.basicPublish(myPersistenceExchange, routingKey, props, "my_perisitence_msg".getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionUtils.closeConnection();
		}
	}
}
