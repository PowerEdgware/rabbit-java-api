package com.api.returnlistener;

import com.api.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ReturnListenerProducer {



	static String QUEUE_NAME="returnlistener_queue";
	public static void main(String[] args) {

		Connection conn=null;
		Channel channel=null;
		try {
			conn=ConnectionUtils.getConnection();
			channel=conn.createChannel();
//			 channel.addConfirmListener(listener);
//			channel.addConfirmListener(ackCallback, nackCallback);
			
			//����Ͷ��ʧ��ʱ�ļ�����(û�취�������)
			//TODO ʧ��ԭ������ǣ����в�����·�ɹؼ��ִ����
			channel.addReturnListener(ret->{
				//�������·���
				System.out.println("returned="+new String(ret.getBody()));
			});
			//TODO ���������� channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			
			//�����ŵ�Ϊȷ��ģʽ
			channel.confirmSelect();//����ȷ��ģʽ
			
			//������Ϣ
			channel.basicPublish("", QUEUE_NAME, true, null, "return_listener_msg".getBytes());
			
			//�ȴ���Ϣȷ��
			//TODO ·�ɵ������ڰ󶨹�ϵ��routingKey
			boolean ok=channel.waitForConfirms(10*1000);
			System.out.println(ok);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			ConnectionUtils.closeConnection();
		}
	
	}

}
