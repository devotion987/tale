package com.tale.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.mvc.http.Request;
import com.tale.ext.Theme;
import com.tale.model.Users;
import com.tale.utils.MapCache;
import com.tale.utils.TaleUtils;

/**
 * Created by biezhi on 2017/2/21.
 */
public abstract class BaseController {

	protected Logger LOGGER = LoggerFactory.getLogger(getClass());

	// public static String THEME = "themes/default";

	protected MapCache cache = MapCache.single();

	public String render(String viewName) {
		return Theme.THEME + "/" + viewName;
	}

	public BaseController title(Request request, String title) {
		request.attribute("title", title);
		return this;
	}

	public BaseController keywords(Request request, String keywords) {
		request.attribute("keywords", keywords);
		return this;
	}

	public Users user() {
		return TaleUtils.getLoginUser();
	}

	public Integer getUid() {
		return this.user().getUid();
	}

	public String render_404() {
		return "/comm/error_404";
	}

}
