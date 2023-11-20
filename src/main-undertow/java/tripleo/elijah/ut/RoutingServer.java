package tripleo.elijah.ut;

import com.stubbornjava.examples.undertow.routing.ConstantStringHandler;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingServer {

	private static final Logger logger = LoggerFactory.getLogger(RoutingServer.class);
	private static final UT_Root utr = new UT_Root();
	private static final HttpHandler ROUTES = new RoutingHandler()
	  .get("/", new CompilationsListHandler(utr))
	  .get("/start/*", new CompilationsStartHandler(utr))
	  .get("/do/*", new DoContinuationsHandler(utr))
	  .post("/myOtherRoute", RoutingHandlers.constantStringHandler("GET - My Other Route"))
	  // Wildcards and RoutingHandler had some bugs before version 1.4.8.Final
	  .get("/myRoutePrefix*", RoutingHandlers.constantStringHandler("GET - My Prefixed Route"))
	  // Pass a handler as a method reference.
	  .setFallbackHandler(RoutingHandlers::notFoundHandler);

	public static void main(final String[] args) {
		final SimpleServer server = SimpleServer.simpleServer(ROUTES);
//        Undertow.Builder undertow = server.getUndertow();
		server.start();
	}

	public static class SimpleServer {
		private static final Logger logger       = LoggerFactory.getLogger(SimpleServer.class);
		private static final int    DEFAULT_PORT = 9898;
		private static final String DEFAULT_HOST = "localhost";

		private final Undertow.Builder undertowBuilder;

		private SimpleServer(final Undertow.Builder undertow) {
			this.undertowBuilder = undertow;
		}

		public static SimpleServer simpleServer(final HttpHandler handler) {
			final Undertow.Builder undertow = Undertow.builder()
			                                          /*
			                                     * This setting is needed if you want to allow '=' as a value in a cookie.
			                                     * If you base64 encode any cookie values you probably want it on.
			                                     */
			                                          .setServerOption(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, true)
			                                          // Needed to set request time in access logs
			                                          .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
			                                          .addHttpListener(DEFAULT_PORT, DEFAULT_HOST, handler);
			return new SimpleServer(undertow);
		}

		/*
		 * As you can see this class is not meant to abstract away the Undertow server,
		 * its goal is simply to have some common configurations. We expose Undertow
		 * if a different service needs to modify it in any way before we call start.
		 */
		public Undertow.Builder getUndertow() {
			return undertowBuilder;
		}

		public Undertow start() {
			final Undertow undertow = undertowBuilder.build();
			undertow.start();
			/*
			 *  Undertow logs this on debug but we generally set 3rd party
			 *  default logger levels to info so we log it here. If it wasn't using the
			 *  io.undertow context we could turn on just that logger but no big deal.
			 */
			undertow.getListenerInfo()
			        .stream()
			        .forEach(listenerInfo -> logger.info(listenerInfo.toString()));
			return undertow;
		}
	}

	public class RoutingHandlers {

		// {{start:handler}}
		/*
		 * Creating static factory methods to construct handlers let's you keep
		 * them better organized and reduce some boilerplate. This will be shown
		 * in future examples.
		 */
		public static HttpHandler constantStringHandler(final String value) {
			return new ConstantStringHandler(value);
		}
		// {{end:handler}}

		// {{start:anonymous}}
		/*
		 * Alternate way to create constantStringHandler using an anonymous HttpHandler.
		 * This is fine for simple handlers but more complex ones might be better off
		 * in their own file.
		 */
		public static HttpHandler constantStringHandlerAlt(final String value) {
			return (final HttpServerExchange exchange) -> {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(value);
			};
		}
		// {{end:anonymous}}

		// {{start:method}}
		/*
		 * This is a 3rd approach to creating HttpHandlers and is heavily utilized
		 * in StubbornJava. We can use Java 8 method references for this approach.
		 * Notice how the void return type and single HttpServerExchange parameter
		 * fulfill the HttpHandler interface.
		 *
		 * This approach is most commonly used for the actual business logic and generally
		 * is responsible for sending the response. Any handlers that chain / delegate
		 * to other handlers are not a great fit for this style.
		 */
		public static void notFoundHandler(final HttpServerExchange exchange) {
			exchange.setStatusCode(404);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.getResponseSender().send("Page Not Found!!");
		}
		// {{end:method}}
	}
}
