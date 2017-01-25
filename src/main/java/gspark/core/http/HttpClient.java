package gspark.core.http;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import gspark.core.config.HttpClientConfig;

public class HttpClient {

	private CloseableHttpClient client;

	public HttpClient(HttpClientConfig config) {
		HttpClientBuilder builder = HttpClients.custom();
		if (config.getProxyHost() != null) {
			builder.setProxy(new HttpHost(config.getProxyHost(), config.getProxyPort()));
		}
		client = builder.build();
	}

	public CloseableHttpResponse execute(HttpUriRequest req) throws IOException {
		return client.execute(req);
	}

}
