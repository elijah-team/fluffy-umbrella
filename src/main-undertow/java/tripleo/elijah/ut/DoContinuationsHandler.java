package tripleo.elijah.ut;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import tripleo.elijah.comp.ICompilationBus;

public class DoContinuationsHandler implements HttpHandler {

	private final UT_Root utr;

	public DoContinuationsHandler(final UT_Root aUtr) {
		utr = aUtr;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");

		final StringBuilder sb  = new StringBuilder();
		final String        num = (exchange.getRequestPath().substring(4));

		final ICompilationBus.CB_Action x = utr.dcs(num);
		x.execute();

		sb.append("<html><body><pre>\n");

		for (final ICompilationBus.OutputString outputString : x.outputStrings()) {
			sb.append("" + outputString.getText() + "\n");
		}

		sb.append("<html><body><pre>\n");


//			sb.append(MessageFormat.format("<tr><td>{0}</td><td>\n", i + 1));
//			sb.append(MessageFormat.format("<a href=\"/start/{0}\">{1}</a>\n", i + 1, x));
//			sb.append("</td></tr>\n");

		sb.append("</pre><table>\n");
		sb.append("</table></body></html>\n");

		exchange.getResponseSender().send(sb.toString());
	}

}
