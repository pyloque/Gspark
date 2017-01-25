package gspark.core.view;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gspark.core.error.ViewError;
import spark.Service;

public abstract class ViewBase {
	private final static Logger LOG = LoggerFactory.getLogger(ViewBase.class);

	private Configuration freemarker;

	protected String render(Object value) {
		return JSON.toJSONString(value);
	}

	public String renderOk(Object value) {
		Map<String, Object> result = new HashMap<>();
		result.put("code", 0);
		result.put("msg", "ok");
		result.put("body", value);
		return JSON.toJSONString(result);
	}

	public String renderOk() {
		return this.render("");
	}

	public String renderTemplate(String template, Object context) {
		try {
			StringWriter stringWriter = new StringWriter();
			Template tpl = freemarker().getTemplate(template);
			tpl.process(context, stringWriter);
			return stringWriter.toString();
		} catch (IOException | TemplateException e) {
			LOG.error("render template error {}", template, e);
			throw new ViewError("render template error", e);
		}
	}

	public String renderTemplate(String template) {
		return this.renderTemplate(template, null);
	}

	private Configuration freemarker() {
		if (freemarker == null) {
			freemarker = new Configuration(Configuration.VERSION_2_3_23);
			freemarker.setClassForTemplateLoading(this.getClass(), "/pages");
		}
		return freemarker;
	}

	public abstract void setup(Service spark);

}
