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

		// ���ӵ�ַ�Ͷ˿�
		factory.setHost(Constants.RABBITMQ_HOST);
		factory.setPort(Constants.RABBITMQ_PORT);
		// ��������
		factory.setVirtualHost(Constants.RABBITMQ_VIRTUAL_HOST);
		factory.setAutomaticRecoveryEnabled(true);
		// �˻���Ϣ
		factory.setUsername(Constants.RABBITMQ_USER);
		factory.setPassword(Constants.RABBITMQ_PASSWD);

		// ��������
		Connection conn = null;
		try {
			conn = factory.newConnection();
			// ����ͨ��
			Channel chn = conn.createChannel();
			// ����������
			// chn.exchangeDeclare(exchange, type)

			// TODO ͨ������������Ϣ����ʱ��
			// Map<String,Object> maps=ImmutableMap.of("x-message-ttl", 10000);
			// ����������Ϣ
			// chn.queueDeclare(QUEUE_NAME, false, false, false, maps);
			// TODO ͨ����Ϣ����������Ϣ����ʱ��
			chn.queueDeclare(QUEUE_NAME, false, false, false, null);
			int i = 0;
			for (;;) {
				// ������Ϣ��ûָ�����������͵�Ĭ��ֱ��������AMQP Default��Direct��
				String msg = ("hello world" + (i++));
				// ������Ϣttl����
				BasicProperties properties = new BasicProperties().builder().deliveryMode(2).expiration("10000")// ����ʱ�䣺����
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
