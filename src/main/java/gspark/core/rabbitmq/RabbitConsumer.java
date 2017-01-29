package gspark.core.rabbitmq;

import com.rabbitmq.client.GetResponse;

import gspark.core.Holder;

public class RabbitConsumer {

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
		store.channel(channel -> {
			channel.exchangeDeclare(exchangeName, exchangeType, exchangeDurable);
		});
		exchangeDeclared = true;
	}

	private void declareQueue() {
		store.channel(channel -> {
			channel.queueDeclare(queueName, queueDurable, queueExclusive, queueAutoDelete, null);
			channel.queueBind(queueName, exchangeName, routingKey);
		});
		queueDeclared = true;
	}

	public RabbitDelivery poll(boolean autoAck) {
		if (!exchangeDeclared) {
			this.declareExchange();
		}
		if (!this.queueDeclared) {
			this.declareQueue();
		}
		Holder<RabbitDelivery> holder = new Holder<>();
		store.channel(channel -> {
			GetResponse res = channel.basicGet(queueName, autoAck);
			if (res != null) {
				RabbitDelivery delivery = new RabbitDelivery(channel, res.getProps(), res.getBody());
				holder.set(delivery);
			}
		});
		return holder.value();
	}

}
