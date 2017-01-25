package ackern.core.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import ackern.core.error.RabbitError;

public class RabbitConsumer {

	private final static Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);

	private RabbitStore store;
	private String exchangeName;
	private String exchangeType;
	private boolean exchangeDurable;
	private volatile boolean exchangeDeclared;

	private String queueName;
	private String routingKey;
	private boolean queueDurable = true;
	private boolean queueExclusive = false;
	private boolean queueAutoDelete = false;
	private volatile boolean queueDeclared;

	public RabbitConsumer(RabbitStore store) {
		this.store = store;
	}

	public RabbitConsumer exchange(String exchangeName, ExchangeType type, boolean durable) {
		this.exchangeName = exchangeName;
		this.exchangeType = type.value();
		this.exchangeDurable = durable;
		return this;
	}

	public RabbitConsumer queue(String queueName, String routingKey) {
		this.queueName = queueName;
		this.routingKey = routingKey;
		return this;
	}

	private void declareExchange() {
		Channel channel = store.channel();
		try {
			channel.exchangeDeclare(exchangeName, exchangeType, exchangeDurable);
		} catch (IOException e) {
			LOG.error("exchange declared error", e);
			throw new RabbitError("declare exchange error", e);
		}
		exchangeDeclared = true;
	}

	private void declareQueue() {
		Channel channel = store.channel();
		try {
			channel.queueDeclare(queueName, queueDurable, queueExclusive, queueAutoDelete, null);
			channel.queueBind(queueName, exchangeName, routingKey);
		} catch (IOException e) {
			LOG.error("queue declared error", e);
			throw new RabbitError("declare queue error", e);
		}
		queueDeclared = true;
	}

	public RabbitDelivery poll(boolean autoAck) {
		if (!exchangeDeclared) {
			this.declareExchange();
		}
		if (!this.queueDeclared) {
			this.declareQueue();
		}
		Channel channel = store.channel();
		try {
			GetResponse res = channel.basicGet(queueName, autoAck);
			if(res == null) {
				return null;
			}
			RabbitDelivery delivery = new RabbitDelivery(channel, res.getProps(), res.getBody());
			return delivery;
		} catch (IOException e) {
			LOG.error("queue fetch error", e);
			throw new RabbitError("fetch queue error", e);
		}
	}

}
