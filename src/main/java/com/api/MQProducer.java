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
			// ����������Ϣ
			chn.queueDeclare(QUEUE_NAME, false, false, false, null);
			int i = 0;
			for (;;) {
				// ������Ϣ��ûָ�����������͵�Ĭ��ֱ��������AMQP Default��Direct��
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
