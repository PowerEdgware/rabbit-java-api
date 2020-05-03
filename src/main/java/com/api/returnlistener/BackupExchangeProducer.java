package com.api.returnlistener;

import java.util.Map;

import com.api.ConnectionUtils;
import com.google.common.collect.Maps;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class BackupExchangeProducer {

	private static final String QUEUE_NAME = "backupExch_queue";

	public static void main(String[] args) {

		// ��ʾ���������У�������Ϣ�޷�·����ô��Ϣ�ᱻ����Ȼ���Զ�����·�ɵ����ݽ�����
		Connection conn = null;
		Channel channel = null;
		try {
			conn = ConnectionUtils.getConnection();
			channel = conn.createChannel();
			// ����Ͷ��ʧ��ʱ�ļ�����(û�취�������)
			// TODO ʧ��ԭ������ǣ����в�����·�ɹؼ��ִ����
			channel.addReturnListener(ret -> {
				// �������·��͵��������л��߽�����
				System.out.println("returned=" + new String(ret.getBody()));
			});
			// TODO ���������� channel.queueDeclare(QUEUE_NAME, true, false, false, null);

			// �����ŵ�Ϊȷ��ģʽ
			channel.confirmSelect();// ����ȷ��ģʽ

			// �ȴ������ݽ�����
			String backup_exchange = "backup_exchange";
			channel.exchangeDeclare(backup_exchange, BuiltinExchangeType.DIRECT, true);
			//�뱸�ݽ������󶨵�queue������δ����ȷ·�ɵ���Ϣ
			String backUpQueue="backup_queue";
			channel.queueDeclare(backUpQueue, true, false, false, null);
			
			//���ݽ������󶨶���
			//TODO ָ���˱��ݽ���������Ϣ�ᱻ·�ɵ��˶��У���ʱ��Ϣ���ᱻ���أ���������returnListener���ٻᱻ����
			channel.queueBind(backUpQueue, backup_exchange, QUEUE_NAME, null);

			// �����ɲο�UI����exchangeʱ�Ĳ�����http://139.196.85.101:17652/#/exchanges
			Map<String, Object> arguments = Maps.newLinkedHashMap();
			// If messages to this exchange cannot otherwise be routed, send them to the
			// alternate exchange named here.
			// (Sets the "alternate-exchange" argument.)
			String testExchange = "test_exchange";
			// ָ�� testExchange�ı��ݽ�����
			arguments.put("alternate-exchange", backup_exchange);

			channel.exchangeDeclare(testExchange, BuiltinExchangeType.TOPIC, false, false, false, arguments);

			// ������Ϣ
			channel.basicPublish(testExchange, QUEUE_NAME, true, null,
					"return_listener_msg_with_back_exchange".getBytes());

			// �ȴ���Ϣȷ��
			boolean ok = channel.waitForConfirms(10 * 1000);
			System.out.println(ok);//true
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionUtils.closeConnection();
		}

	}
}
