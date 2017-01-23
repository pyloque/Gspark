package ackern.core.rabbitmq;

import com.google.common.base.Charsets;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

public class RabbitDelivery {
	private Channel channel;
	private BasicProperties header;
	private byte[] body;
	private String content;

	public RabbitDelivery(Channel channel, BasicProperties header, byte[] body) {
		this.channel = channel;
		this.header = header;
		this.body = body;
	}

	public Channel channel() {
		return channel;
	}

	public BasicProperties header() {
		return header;
	}

	public byte[] body() {
		return body;
	}

	public String content() {
		if (content == null) {
			content = new String(body, Charsets.UTF_8);
		}
		return content;
	}

}
