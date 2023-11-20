package com.stubbornjava.common.undertow;

import com.stubbornjava.undertow.exchange.ContentTypeSenders;
import com.stubbornjava.undertow.exchange.PathParams;
import com.stubbornjava.undertow.exchange.QueryParams;
import com.stubbornjava.undertow.exchange.RedirectSenders;

/*
 * Using static globals for simplicity. Use your own DI method however you want.
 */
public class Exchange {
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

	public static RedirectImpl redirect() {
		return REDIRECT;
	}

	public static QueryParamImpl queryParams() {
		return QUERYPARAMS;
	}

	public static PathParamImpl pathParams() {
		return PATHPARAMS;
	}

	;

	public static UrlImpl urls() {
		return URLS;
	}

	public static HeaderImpl headers() {
		return HEADERS;
	}

	public static interface BodyImpl extends
	  ContentTypeSenders
	  , JsonSender
	  , JsonParser
	  , HtmlTemplateSender {
	}

	;
	public static interface RedirectImpl extends RedirectSenders {
	}

	public static interface QueryParamImpl extends QueryParams {
	}

	public static interface PathParamImpl extends PathParams {
	}

	;
	public static interface UrlImpl extends Urls {
	}

	public static interface HeaderImpl extends Headers {
	}

}
