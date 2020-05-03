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

		// 演示不创建队列，导致消息无法路由那么消息会被返回然后自动重新路由到备份交换机
		Connection conn = null;
		Channel channel = null;
		try {
			conn = ConnectionUtils.getConnection();
			channel = conn.createChannel();
			// 设置投递失败时的监听器(没办法进入队列)
			// TODO 失败原因可能是，队列不存在路由关键字错误等
			channel.addReturnListener(ret -> {
				// 可以重新发送到其他队列或者交换机
				System.out.println("returned=" + new String(ret.getBody()));
			});
			// TODO 不创建队列 channel.queueDeclare(QUEUE_NAME, true, false, false, null);

			// 设置信道为确认模式
			channel.confirmSelect();// 开启确认模式

			// 先创建备份交换机
			String backup_exchange = "backup_exchange";
			channel.exchangeDeclare(backup_exchange, BuiltinExchangeType.DIRECT, true);
			//与备份交换机绑定的queue，处理未被正确路由的消息
			String backUpQueue="backup_queue";
			channel.queueDeclare(backUpQueue, true, false, false, null);
			
			//备份交换机绑定队列
			//TODO 指定了备份交换机的消息会被路由到此队列，此时消息不会被返回，所以上述returnListener不再会被调用
			channel.queueBind(backUpQueue, backup_exchange, QUEUE_NAME, null);

			// 参数可参考UI创建exchange时的参数：http://139.196.85.101:17652/#/exchanges
			Map<String, Object> arguments = Maps.newLinkedHashMap();
			// If messages to this exchange cannot otherwise be routed, send them to the
			// alternate exchange named here.
			// (Sets the "alternate-exchange" argument.)
			String testExchange = "test_exchange";
			// 指定 testExchange的备份交换机
			arguments.put("alternate-exchange", backup_exchange);

			channel.exchangeDeclare(testExchange, BuiltinExchangeType.TOPIC, false, false, false, arguments);

			// 发送消息
			channel.basicPublish(testExchange, QUEUE_NAME, true, null,
					"return_listener_msg_with_back_exchange".getBytes());

			// 等待消息确认
			boolean ok = channel.waitForConfirms(10 * 1000);
			System.out.println(ok);//true
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionUtils.closeConnection();
		}

	}
}
