package ackern.core.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.rabbitmq.client.AMQP.BasicProperties;

import ackern.core.error.RabbitError;

import com.rabbitmq.client.Channel;

public class RabbitPublisher {
	private final static Logger LOG = LoggerFactory.getLogger(RabbitPublisher.class);

	private RabbitStore store;
	private String exchangeName;
	private String exchangeType;
	private boolean exchangeDurable;
	private boolean exchangeDeclared;
	private String defaultRoutingKey;
	private BasicProperties defaultHeader;

	public RabbitPublisher(RabbitStore store) {
		this.store = store;
	}

	public RabbitPublisher exchange(String exchangeName, ExchangeType type, boolean durable) {
		this.exchangeName = exchangeName;
		this.exchangeType = type.value();
		this.exchangeDurable = durable;
		return this;
	}

	public RabbitPublisher defaultRoutingKey(String routingKey) {
		this.defaultRoutingKey = routingKey;
		return this;
	}

	public RabbitPublisher defaultHeader(BasicProperties header) {
		this.defaultHeader = header;
		return this;
	}

	private void declareExchange() {
		Channel channel = store.channel();
		try {
			channel.exchangeDeclare(exchangeName, exchangeType, exchangeDurable);
			exchangeDeclared = true;
		} catch (IOException e) {
			LOG.error("queue declared error", e);
			throw new RabbitError("declare queue error", e);
		}
	}

	public void publish(String routingKey, BasicProperties header, byte[] body) {
		if (!exchangeDeclared) {
			this.declareExchange();
		}
		Channel channel = store.channel();
		try {
			channel.basicPublish(exchangeName, routingKey, header, body);
		} catch (IOException e) {
			LOG.error("publish to rabbitmq error", e);
			throw new RabbitError("publish to exchange error", e);
		}
	}

	public void publish(String routingKey, BasicProperties header, String content) {
		this.publish(routingKey, header, content.getBytes(Charsets.UTF_8));
	}

	public void publish(String routingKey, byte[] body) {
		if (defaultHeader == null) {
			defaultHeader = headerBuilder().build();
		}
		this.publish(routingKey, defaultHeader, body);
	}

	public void publish(String routingKey, String content) {
		this.publish(routingKey, content.getBytes(Charsets.UTF_8));
	}

	public void publish(byte[] body) {
		String routingKey = "";
		if (defaultRoutingKey != null) {
			routingKey = defaultRoutingKey;
		}
		this.publish(routingKey, body);
	}

	public void publish(String content) {
		this.publish(content.getBytes(Charsets.UTF_8));
	}

	public static BasicProperties.Builder headerBuilder() {
		return new BasicProperties.Builder();
	}

}
