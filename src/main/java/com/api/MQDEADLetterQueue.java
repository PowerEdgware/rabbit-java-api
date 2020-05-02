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
 * ��Ϣ������ŵ�������
 *1.The message is negatively acknowledged by a consumer using basic.reject or basic.nack 
 *with requeue parameter set to false.
2.The message expires due to per-message TTL; or
3.The message is dropped because its queue exceeded a length limit
 *
 */
public class MQDEADLetterQueue {

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

			// ָ����������ʱ��ʹ�õ����Ž�����
			// ͨ������ָ��
			String deadLetterExchange="DLX_EXCHANGE";
			Map<String, Object> mapArgs = ImmutableMap.of("x-dead-letter-exchange", deadLetterExchange,
					"x-dead-letter-routing-key", "DLX_KEY");
			//������ͨ����ʱָ�����Ž��������Ա㶪ʧ����Ϣ���Ա�Ͷ�ݵ����Ž�����
			chn.queueDeclare("MY_DEMO_QUEUE", false, false, false, mapArgs);
			
			//�������Ž������������Ŷ���
			chn.exchangeDeclare(deadLetterExchange, "topic", false, false, true, null);
			String dlxQueue="DLX_QUEUE";
			chn.queueDeclare(dlxQueue, false, false, false, null);
			
			//�����Ŷ��е����Ž�����
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
