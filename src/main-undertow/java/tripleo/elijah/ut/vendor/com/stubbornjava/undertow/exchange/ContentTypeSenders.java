package tripleo.elijah.ut.vendor.com.stubbornjava.undertow.exchange;

import java.nio.ByteBuffer;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public interface ContentTypeSenders {

	default void sendFile(HttpServerExchange exchange, String fileName, byte[] bytes) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
		exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
		exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
	}

	default void sendFile(HttpServerExchange exchange, String fileName, String content) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
		exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
		exchange.getResponseSender().send(content);
	}

	default void sendHtml(HttpServerExchange exchange, String html) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
		exchange.getResponseSender().send(html);
	}

	default void sendJson(HttpServerExchange exchange, byte[] bytes) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
	}

	default void sendJson(HttpServerExchange exchange, String json) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		exchange.getResponseSender().send(json);
	}

	default void sendText(HttpServerExchange exchange, String text) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		exchange.getResponseSender().send(text);
	}

	default void sendXml(HttpServerExchange exchange, String xml) {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/xml");
		exchange.getResponseSender().send(xml);
	}
}
