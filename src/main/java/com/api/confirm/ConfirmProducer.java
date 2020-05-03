package com.api.confirm;


import com.api.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class ConfirmProducer {

	static String QUEUE_NAME="confirm_queue";
	public static void main(String[] args) {

		Connection conn=null;
		Channel channel=null;
		try {
			conn=ConnectionUtils.getConnection();
			 channel=conn.createChannel();
			channel.queueDeclare(QUEUE_NAME, true, false, false, null);
			
			//�����ŵ�Ϊȷ��ģʽ
			channel.confirmSelect();//����ȷ��ģʽ
			
			//������Ϣ
			channel.basicPublish("", QUEUE_NAME, null, "confirm_msg".getBytes());
			
			//�ȴ���Ϣȷ��
			boolean ok=channel.waitForConfirms(10*1000);
			System.out.println(ok);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			ConnectionUtils.closeConnection();
		}
	
	}
}
