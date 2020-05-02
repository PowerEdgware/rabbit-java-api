package com.api;

public class DelayedQueueWithDeadLetteredQueue {

	void readme() {
		//TODO 正常的队列设置消息过期时间同时指定死信队列
		//消息过期时直接republish到死信交换机上的死信队列
		//指定某个消费者去消费该死信交换机上的私信队列消息即可
		//TODO 
		//使用rabbitmq-delayed-message-exchange插件。
	}
}
