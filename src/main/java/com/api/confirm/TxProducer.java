package com.api.confirm;

import java.io.IOException;

import com.api.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * 确保消息正确发送到	broker
 * @author User
 *
 */
public class TxProducer {

	public static void main(String[] args) {
		Connection conn=null;
		Channel channel=null;
		try {
			conn=ConnectionUtils.getConnection();
			 channel=conn.createChannel();
			channel.queueDeclare("tx_queue", true, false, false, null);
			//开始事务
			channel.txSelect();//开启事务
			
			channel.basicPublish("", "tx_queue", null, "tx_msg".getBytes());
			
			//提交事务
			channel.txCommit();
		} catch (IOException e) {
			if(channel!=null) {
				try {
					channel.txRollback();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}finally {
			ConnectionUtils.closeConnection();
		}
	}
}
