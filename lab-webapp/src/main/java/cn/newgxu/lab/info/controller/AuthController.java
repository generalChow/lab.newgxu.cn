/*
 * Copyright 2001-2013 newgxu.cn the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.newgxu.lab.info.controller;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
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
	
	private static final Logger L = LoggerFactory.getLogger(AuthController.class);

	@Inject
	private AuthService authService;
	
	@RequestMapping(value = "/auth", method = RequestMethod.POST, produces = AjaxConstants.MEDIA_TYPE_JSON)
	@ResponseBody
	public String auth(AuthorizedUser au) throws JSONException {
		L.info("尝试认证用户！单位（组织）：{}，名称：{}", au.getOrg(), au.getAuthorizedName());
		authService.create(au);
		return AjaxConstants.JSON_STATUS_OK;
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = AjaxConstants.MEDIA_TYPE_JSON)
	@ResponseBody
	public String login(@RequestParam("account") String account, @RequestParam("pwd") String password, HttpServletRequest request) {
		AuthorizedUser au = authService.login(account, password);
		if (au != null) {
			request.getSession().setAttribute(Config.SESSION_USER, au);
		} else {
			return AjaxConstants.JSON_STATUS_NO;
		}
		return AjaxConstants.JSON_STATUS_OK;
	}
	
	@RequestMapping(value = "/logout", produces = AjaxConstants.MEDIA_TYPE_JSON)
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
		return Config.APP + "/update";
	}
	
	@RequestMapping(value = "/user/update", method = RequestMethod.POST)
	@ResponseBody
	public String update(@RequestParam("pwd") String newPwd, HttpSession session) {
		AuthorizedUser au = (AuthorizedUser) session.getAttribute(Config.SESSION_USER);
		au.setPassword(newPwd);
		authService.update(au);
		return AjaxConstants.JSON_STATUS_OK;
	}
	
	@RequestMapping(value = "/user/list/{offset}/{count}", produces = AjaxConstants.MEDIA_TYPE_JSON)
	@ResponseBody
	public String list(@PathVariable("offset") int offset, @PathVariable("count") int count) {
		List<AuthorizedUser> list = authService.list(offset, count);
		return new JSONArray(list).toString();
	}
	
}