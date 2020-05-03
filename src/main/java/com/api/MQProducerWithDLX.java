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
			String QUEUE_NAME="MY_DEMO_QUEUE";
			//send msg
			int i = 0;
			for (;;) {
				// ������Ϣ��ûָ�����������͵�Ĭ��ֱ��������AMQP Default��Direct��
				AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
						.deliveryMode(2) // �־û���Ϣ
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
