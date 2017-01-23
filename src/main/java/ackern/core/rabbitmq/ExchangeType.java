package ackern.core.rabbitmq;

public enum ExchangeType {
	DIRECT("direct"), FANOUT("fanout"), TOPIC("topic");

	private final String value;

	private ExchangeType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

}
