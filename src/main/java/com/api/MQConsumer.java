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

			// ����������Ϣ
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
			chn.basicQos(5);//���������
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
