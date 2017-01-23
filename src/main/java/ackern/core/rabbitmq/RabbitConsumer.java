package ackern.core.rabbitmq;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

import ackern.core.errors.RabbitError;

public class RabbitConsumer {

	private final static Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);

	private RabbitStore store;
	private String exchangeName;
	private String exchangeType;
	private boolean exchangeDurable;
	private boolean exchangeDeclared;

	private String queueName;
	private String routingKey;
	private boolean queueDurable = true;
	private boolean queueExclusive = false;
	private boolean queueAutoDelete = false;
	private boolean queueDeclared;

	private Map<String, Consumer<RabbitDelivery>> consumers = new LinkedHashMap<>();

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

	public RabbitConsumer consumer(String name, Consumer<RabbitDelivery> consumer) {
		this.consumers.put(name, consumer);
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

	public void startConsuming(boolean autoAck) {
		if (!exchangeDeclared) {
			this.declareExchange();
		}
		if (!this.queueDeclared) {
			this.declareQueue();
		}
		Channel channel = store.channel();
		try {
			channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {

				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties header, byte[] body)
						throws IOException {
					RabbitDelivery delivery = new RabbitDelivery(channel, header, body);
					consumeDelivery(delivery);
				}

			});
		} catch (IOException e) {
			LOG.error("queue consumed error", e);
			throw new RabbitError("consume queue error", e);
		}
	}

	public void startChewing(boolean autoAck) {
		if (!exchangeDeclared) {
			this.declareExchange();
		}
		if (this.queueDeclared) {
			this.declareQueue();
		}
		Channel channel = store.channel();
		try {
			GetResponse res = channel.basicGet(queueName, autoAck);
			RabbitDelivery delivery = new RabbitDelivery(channel, res.getProps(), res.getBody());
			this.consumeDelivery(delivery);
		} catch (IOException e) {
			LOG.error("queue fetch error", e);
			throw new RabbitError("fetch queue error", e);
		}
	}

	private void consumeDelivery(RabbitDelivery delivery) {
		for (Entry<String, Consumer<RabbitDelivery>> entry : consumers.entrySet()) {
			String name = entry.getKey();
			Consumer<RabbitDelivery> consumer = entry.getValue();
			try {
				consumer.accept(delivery);
			} catch (Exception e) {
				LOG.error("consume delivery error name={} header={} body={}", name, delivery.header(),
						delivery.content(), e);
			}
		}
	}

	public static void main(String[] args) {
		RabbitStore store = new RabbitStore(URI.create("amqp://guest:guest@localhost:5672"));
		RabbitConsumer consumer = new RabbitConsumer(store);
		consumer.exchange("test-ex", ExchangeType.DIRECT, true).queue("test-q", "test-r");
		consumer.consumer("test", delivery -> {
			System.out.println(delivery.content());
		});
		consumer.startConsuming(true);
	}

}
