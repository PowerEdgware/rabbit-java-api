package com.api;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 利用线程本地变量获取连接
 * @author User
 *
 */
public abstract class ConnectionUtils {

	final static ThreadLocal<ConnectionHolder> connThreadLocal;
	static ConnectionFactory factory;

	static {
		connThreadLocal = new ThreadLocal<ConnectionUtils.ConnectionHolder>();
		factory = new ConnectionFactory();
		// 连接地址和端口
		factory.setHost(Constants.RABBITMQ_HOST);
		factory.setPort(Constants.RABBITMQ_PORT);
		// 虚拟主机
		factory.setVirtualHost(Constants.RABBITMQ_VIRTUAL_HOST);
		factory.setAutomaticRecoveryEnabled(true);
		// 账户信息
		factory.setUsername(Constants.RABBITMQ_USER);
		factory.setPassword(Constants.RABBITMQ_PASSWD);
	}

	public static Connection getConnection() {
		ConnectionHolder holder = connThreadLocal.get();
		if (holder != null) {
			return holder.conn;
		}
		try {
			Connection conn = factory.newConnection();
			holder = wrapperConnection(conn);
			connThreadLocal.set(holder);
			return holder.conn;
		} catch (IOException | TimeoutException e) {
			throw rethrow(e);
		}

	}

	public static void closeConnection() {
		ConnectionHolder holder=connThreadLocal.get();
		if(holder!=null) {
			try {
				holder.chnMap
				.forEach((i,c)->{
					try {
						c.close();
					} catch (IOException | TimeoutException e) {
						e.printStackTrace();
					}
				});
				
				holder.rawConn.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw rethrow(e);
			}
		}
	}
	static RuntimeException rethrow(Exception e) {
		return new RuntimeException(e);
	}

	private static ConnectionHolder wrapperConnection(Connection conn) {
		ConnectionHolder holder=new ConnectionHolder();
		holder.rawConn=conn;
		
		Connection proxied= (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(),
				new Class[] {Connection.class}, new ConnectionInvokeHandler(holder));
		holder.conn=proxied;
		return holder;
	}

	private static final class ConnectionHolder {
		Connection conn;
		Connection rawConn;
		Map<Integer, Channel> chnMap=Maps.newHashMap();
	}

	private static class ConnectionInvokeHandler implements InvocationHandler {
		Connection rawConn;
		ConnectionHolder holder;
		public ConnectionInvokeHandler(ConnectionHolder holder) {
			this.holder=holder;
			this.rawConn=holder.rawConn;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if (method.getName().contentEquals("close")) {
				return null;
			}
			if (methodName.contentEquals("createChannel") || methodName.contentEquals("openChannel")) {
				Channel rawChannel = (Channel) method.invoke(rawConn, args);
				Object proxied= Proxy.newProxyInstance(rawConn.getClass().getClassLoader(), new Class[] { Channel.class },
						new ChannelInvocationHandler(holder, rawChannel));
				this.holder.chnMap.putIfAbsent(rawChannel.getChannelNumber(), rawChannel);
				return proxied;
			}
			return method.invoke(rawConn, args);
		}

	}

	private static class ChannelInvocationHandler implements InvocationHandler {
		private ConnectionHolder holder;
		private Channel rawChannel;

		public ChannelInvocationHandler(ConnectionHolder holder, Channel rawChannel) {
			this.holder = holder;
			this.rawChannel = rawChannel;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if (methodName.contentEquals("close")) {
				return null;
			}
			if (methodName.contains("getConnection")) {
				return this.holder.conn;
			}
			return method.invoke(rawChannel, args);
		}
	}
}
