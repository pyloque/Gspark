package gspark.core.http;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gspark.core.Holder;
import gspark.core.config.HttpClientConfig;
import gspark.core.error.HttpError;

public class HttpClient {
	private final static Logger LOG = LoggerFactory.getLogger(HttpClient.class);

	private CloseableHttpClient http;

	public HttpClient(HttpClientConfig config) {
		HttpClientBuilder builder = HttpClients.custom();
		if (config.getProxyHost() != null) {
			builder.setProxy(new HttpHost(config.getProxyHost(), config.getProxyPort()));
		}
		http = builder.build();
	}

	public void execute(HttpClientOperation<CloseableHttpClient> op) {
		try {
			op.accept(http);
		} catch (IOException e) {
			LOG.error("http client operation error", e);
			throw new HttpError("http client operation error", e);
		}
	}

	public CloseableHttpResponse request(HttpUriRequest req) {
		Holder<CloseableHttpResponse> holder = new Holder<>();
		this.execute(http -> {
			holder.set(http.execute(req));
		});
		return holder.value();
	}

}
