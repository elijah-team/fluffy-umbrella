package tripleo.elijah.ut;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;

public class CompilationsListHandler implements HttpHandler {

	private final UT_Root utr;

	public CompilationsListHandler(final UT_Root aUtr) {
		utr = aUtr;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

		final StringBuilder sb = new StringBuilder();

		sb.append("<html><body><table>\n");

		final List<Path> paths = utr.paths;

		for (int i = 0; i < paths.size(); i++) {
			final Path x = paths.get(i);
			sb.append(MessageFormat.format("<tr><td>{0}</td><td>\n", i + 1));
			sb.append(MessageFormat.format("<a href=\"/start/{0}\">{1}</a>\n", i + 1, x));
			sb.append("</td></tr>\n");
		}

		sb.append("</table></body></html>\n");

		exchange.getResponseSender().send(sb.toString());
	}

}
