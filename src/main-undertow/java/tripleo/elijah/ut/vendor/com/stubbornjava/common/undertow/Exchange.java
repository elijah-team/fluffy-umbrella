package tripleo.elijah.ut.vendor.com.stubbornjava.common.undertow;

import tripleo.elijah.ut.vendor.com.stubbornjava.undertow.exchange.ContentTypeSenders;
import tripleo.elijah.ut.vendor.com.stubbornjava.undertow.exchange.PathParams;
import tripleo.elijah.ut.vendor.com.stubbornjava.undertow.exchange.QueryParams;
import tripleo.elijah.ut.vendor.com.stubbornjava.undertow.exchange.RedirectSenders;

/*
 * Using static globals for simplicity. Use your own DI method however you want.
 */
public class Exchange {
	public static interface BodyImpl extends
	  ContentTypeSenders
	  , JsonSender
	  , JsonParser
	  , HtmlTemplateSender {
	}

	;
	public static interface HeaderImpl extends Headers {
	}
	public static interface PathParamImpl extends PathParams {
	}
	public static interface QueryParamImpl extends QueryParams {
	}

	;
	public static interface RedirectImpl extends RedirectSenders {
	}
	public static interface UrlImpl extends Urls {
	}

	private static final BodyImpl BODY = new BodyImpl() {
	};

	;

	private static final RedirectImpl REDIRECT = new RedirectImpl() {
	};

	private static final QueryParamImpl QUERYPARAMS = new QueryParamImpl() {
	};

	private static final PathParamImpl PATHPARAMS = new PathParamImpl() {
	};

	;

	private static final UrlImpl URLS = new UrlImpl() {
	};

	private static final HeaderImpl HEADERS = new HeaderImpl() {
	};

	public static BodyImpl body() {
		return BODY;
	}

	;
	public static HeaderImpl headers() {
		return HEADERS;
	}

	public static PathParamImpl pathParams() {
		return PATHPARAMS;
	}

	public static QueryParamImpl queryParams() {
		return QUERYPARAMS;
	}

	;
	public static RedirectImpl redirect() {
		return REDIRECT;
	}

	public static UrlImpl urls() {
		return URLS;
	}

}
