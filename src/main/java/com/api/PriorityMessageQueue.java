package com.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class PriorityMessageQueue {

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
			// ͨ������ָ�����е����ȼ�������Ϣʱָ����Ϣ�����ȼ�x-max-priority
			Map<String, Object> mapArgs = ImmutableMap.of("x-max-priority", 10);
			chn.queueDeclare("my_priority_queue", true, false, false, mapArgs);
			
			//��Ϣ�ѻ�ʱ�����ȼ��ߵ���Ϣ�������ȱ�����
			AMQP.BasicProperties props=new AMQP.BasicProperties().builder()
					.deliveryMode(2)
					.priority(7)
					.build();
			chn.basicPublish("", "my_priority_queue", props, "prority_msg".getBytes());
			
			chn.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
