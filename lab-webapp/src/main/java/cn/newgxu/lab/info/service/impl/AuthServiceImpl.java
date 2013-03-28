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
package cn.newgxu.lab.info.service.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.newgxu.lab.core.util.DateTime;
import cn.newgxu.lab.core.util.Encryptor;
import cn.newgxu.lab.info.config.Config;
import cn.newgxu.lab.info.entity.AuthorizedUser;
import cn.newgxu.lab.info.repository.AuthDao;
import cn.newgxu.lab.info.service.AuthService;

/**
 * 认证用户对外服务的实现。
 * 
 * @author longkai
 * @email im.longkai@gmail.com
 * @since 2013-3-28
 * @version 0.1
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Throwable.class)
public class AuthServiceImpl implements AuthService {

	private static final Logger	L	= LoggerFactory
											.getLogger(AuthServiceImpl.class);

	@Inject
	private AuthDao				authDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void create(AuthorizedUser user) {
		// 设置密码
		if (user.getPassword() != null) {
			String plainText = user.getPassword().trim();
			if (plainText.equals("")) {
				throw new RuntimeException("密码不能为空！");
			}
			user.setPassword(Encryptor.MD5(plainText));
		} else {
			user.setPassword(Encryptor.MD5(Config.DEFAULT_PASSWORD));
		}

		// 设置账号
		if (user.getAccount() == null) {
			// 如果是没有指定账号，那么我们就按照时间来，这样肯定不会有重复
			user.setAccount(Config.DEFAULT_ACCOUNT_PREFIX + DateTime.format(new Date(), Config.DEFAULT_ACCOUNT_SUFFIX));
		} else {
//			检查账号名是否存在！
			if (authDao.has(user.getAccount())) {
				throw new RuntimeException("用户名已经存在！");
			}
		}
		
		if (user.getOrg() == null || user.getOrg().trim().equals("")) {
			throw new RuntimeException("组织或者单位不能为空！");
		}

		if (user.getAuthorizedName() == null || user.getAuthorizedName().trim().equals("")) {
			throw new RuntimeException("显示名称不能为空！");
		}

		user.setJoinTime(new Date());

		authDao.persist(user);

		L.info("用户：{} 注册成功！id：{}，账号：{}， org：{}", user.getAuthorizedName(),
				user.getId(), user.getAccount(), user.getOrg());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public AuthorizedUser update(AuthorizedUser user) {
		AuthorizedUser u = authDao.find(user.getId());
		u.setPassword(Encryptor.MD5(user.getPassword()));
		authDao.merge(u);
		L.info("用户：{} 修改信息成功！", user.getAuthorizedName());
		return u;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void block(AuthorizedUser user) {
		user.setBlocked(true);
		authDao.merge(user);
	}

	@Override
	public AuthorizedUser find(long pk) {
		return authDao.find(pk);
	}

	@Override
	public AuthorizedUser login(String account, String password) {
		L.info("用户:{} 登录", account);
		AuthorizedUser user = null;
		try {
			user = authDao.find(account, Encryptor.MD5(password));
		} catch (Exception e) {
			L.error("用户登录异常！", e);
			throw new RuntimeException("用户登录异常！", e);
		}
		return user;
	}

	@Override
	public long total() {
		return authDao.size();
	}

	@Override
	public boolean exists(String account) {
		return authDao.has(account);
	}

	@Override
	public List<AuthorizedUser> list(int NO, int howMany) {
		return authDao.list(NO, Config.DEFAULT_USER_LIST_COUNT);
	}

}