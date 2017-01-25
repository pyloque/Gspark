package gspark.core.rabbitmq;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import gspark.core.config.RabbitConfig;
import gspark.core.error.RabbitError;

public class RabbitStore implements Closeable {
	private final static Logger LOG = LoggerFactory.getLogger(RabbitStore.class);

	private ConnectionFactory factory;
	private Connection conn;
	private ThreadLocal<Channel> channelHolder = new ThreadLocal<>();

	public RabbitStore(RabbitConfig config) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setConnectionTimeout(config.getConnectTimeout());
		URI uri;
		try {
			uri = new URI(config.getUri());
		} catch (URISyntaxException e) {
			LOG.error("illegal rabbitmq uri {}", config.getUri(), e);
			throw new RabbitError("illegal rabbitmq uri", e);
		}
		try {
			factory.setUri(uri);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
			LOG.error("rabbitmq uri format illegal", e);
			throw new RabbitError("rabbitmq uri format illegal", e);
		}
		this.factory = factory;
	}

	public void connect() {
		try {
			this.conn = factory.newConnection();
		} catch (IOException | TimeoutException e) {
			LOG.error("rabbitmq uri connect failure", e);
			throw new RabbitError("rabbitmq uri connect failure", e);
		}
	}

	public Channel channel() {
		Channel channel = channelHolder.get();
		if (channel != null) {
			return channel;
		}
		if (this.conn == null) {
			connect();
		}
		try {
			channel = this.conn.createChannel();
		} catch (IOException e) {
			LOG.error("rabbitmq create channel failure", e);
			throw new RabbitError("rabbitmq create channel failure", e);
		}
		channelHolder.set(channel);
		return channel;
	}

	@Override
	public void close() throws IOException {
		if (this.conn != null) {
			this.conn.close();
		}
	}

}
