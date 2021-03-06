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
package cn.newgxu.lab.info.repository.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import cn.newgxu.lab.core.repository.impl.AbstractCommonDaoImpl;
import cn.newgxu.lab.info.entity.Notice;
import cn.newgxu.lab.info.repository.NoticeDao;

/**
 * 信息发布的数据访问实现。
 * 
 * @author longkai
 * @email im.longkai@gmail.com
 * @since 2013-3-28
 * @version 0.1
 */
@Repository
public class NoticeDaoImpl extends AbstractCommonDaoImpl<Notice> implements NoticeDao {

	@Override
	public Notice find(Serializable pk) {
		return super.em.find(Notice.class, pk);
	}

	@Override
	public long size() {
		return super.size(Notice.class);
	}

	@Override
	public List<Notice> list(String query, Map<String, Object> params, int offset, int number) {
		return super.executeQuery(query, Notice.class, offset, number, params);
	}

	@Override
	public int newerCount(long pk) {
		long l = 0L;
		try {
			l = em.createNamedQuery("Notice.newer_count", Long.class)
					.setParameter("last_id", pk)
					.getSingleResult();
		} catch (Exception e) {
			L.error("查询是否有更新时异常！", e);
		}
		return Long.valueOf(l).intValue();
	}

}
