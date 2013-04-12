/*
 * Copyright (c) 2001-2013 newgxu.cn <the original author or authors>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package cn.newgxu.lab.info.controller;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.newgxu.lab.core.common.AjaxConstants;
import cn.newgxu.lab.info.config.Config;
import cn.newgxu.lab.info.entity.AuthorizedUser;
import cn.newgxu.lab.info.service.AuthService;

/**
 * 信息发布平台认证用户的主控制器。
 * 
 * @author longkai
 * @email im.longkai@gmail.com
 * @since 2013-3-29
 * @version 0.1
 */
@Controller
@RequestMapping("/" + Config.APP)
@Scope("session")
public class AuthController {

	private static final Logger	L =
			LoggerFactory.getLogger(AuthController.class);

	@Inject
	private AuthService			authService;

	@RequestMapping(
		value	 = "/auth",
		method	 = RequestMethod.POST,
		produces = AjaxConstants.MEDIA_TYPE_JSON
	)
	@ResponseBody
	public String auth(
			AuthorizedUser au,
			@RequestParam(value = "_pwd", defaultValue = "") String _pwd)
					throws JSONException {
		L.info("尝试认证用户！单位（组织）：{}，名称：{}，密码：{}",
				au.getOrg(), au.getAuthorizedName(), au.getPassword());
		authService.create(au, _pwd);
		JSONObject json = new JSONObject(au);
		json.put(AjaxConstants.AJAX_STATUS, "ok");
		return json.toString();
	}

	@RequestMapping(
		value	 = "/login",
		method	 = RequestMethod.POST,
		produces = AjaxConstants.MEDIA_TYPE_JSON
	)
	@ResponseBody
	public String login(
			@RequestParam("account") String account,
			@RequestParam("pwd") String password,
			HttpServletRequest request) {
		AuthorizedUser au = authService.login(account, password);
		if (au != null) {
			request.getSession().setAttribute(Config.SESSION_USER, au);
		} else {
			return AjaxConstants.JSON_STATUS_NO;
		}
		return AjaxConstants.JSON_STATUS_OK;
	}

	@RequestMapping(
		value	 = "/logout",
		produces = AjaxConstants.MEDIA_TYPE_JSON
	)
	@ResponseBody
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return AjaxConstants.JSON_STATUS_OK;
	}

	@RequestMapping(value = "/user/update/{uid}", method = RequestMethod.GET)
	public String update(@PathVariable("uid") int uid, Model model) {
		AuthorizedUser au = authService.find(uid);
		model.addAttribute("au", au);
		return Config.APP + "/update_user";
	}

	@RequestMapping(value = "/user/update/{type}", method = RequestMethod.POST)
	@ResponseBody
	public String resetPwd(@PathVariable("type") String type,
			@RequestParam("password") String password,
			@RequestParam(value = "contact", required = false) String contact,
			@RequestParam(value = "about", required = false) String about,
			@RequestParam(value = "pwd1", required = false) String pwd1,
			@RequestParam(value = "pwd2", required = false) String pwd2,
			HttpSession session) {
		AuthorizedUser sau = (AuthorizedUser) session
				.getAttribute(Config.SESSION_USER);
//		首先验证一下密码是否正确。
		if (authService.login(sau.getAccount(), password) == null) {
			throw new IllegalArgumentException("原来的密码错误！");
		}
		if (type.equals("password")) {
			sau.setPassword(pwd1);
			sau = authService.resetPassword(sau, pwd2);
		} else if (type.equals("profile")) {
			sau.setContact(contact);
			sau.setAbout(about);
			sau = authService.update(sau);
		} else {
			throw new UnsupportedOperationException("不存在的操作！");
		}
		// 重新把更新后的user设置到session中。
		session.setAttribute(Config.SESSION_USER, sau);
		return AjaxConstants.JSON_STATUS_OK;
	}

	@RequestMapping(
		value	 = "/user/profile/{uid}",
		produces = AjaxConstants.MEDIA_TYPE_JSON
	)
	@ResponseBody
	public String profile(@PathVariable("uid") long uid) {
		AuthorizedUser au = authService.find(uid);
		return new JSONObject(au).toString();
	}

	@RequestMapping(
		value	 = "/user/list/{last_uid}/{count}",
		produces = AjaxConstants.MEDIA_TYPE_JSON
	)
	@ResponseBody
	public String list(@PathVariable("last_uid") long lastUid,
			@PathVariable("count") int count) {
		List<AuthorizedUser> list = authService.more(lastUid, count);
//		直接用list构造json失败的话可以使用这个构造方法。
		return new JSONArray(list, false).toString();
	}
	
}