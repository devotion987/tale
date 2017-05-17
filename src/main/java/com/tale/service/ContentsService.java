package com.tale.service;

import java.util.List;

import com.blade.ioc.annotation.Inject;
import com.blade.ioc.annotation.Service;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.jdbc.model.PageRow;
import com.blade.jdbc.model.Paginator;
import com.blade.kit.DateKit;
import com.blade.kit.StringKit;
import com.tale.constants.TaleConst;
import com.tale.dto.Types;
import com.tale.exception.TipException;
import com.tale.model.Contents;
import com.tale.model.Users;
import com.tale.service.ContentsService;
import com.tale.utils.TaleUtils;
import com.vdurmont.emoji.EmojiParser;

@Service
public class ContentsService {

	@Inject
	private ActiveRecord activeRecord;

	@Inject
	private MetasService metasService;

	public Contents getContents(String id) {
		if (StringKit.isNotBlank(id)) {
			if (StringKit.isNumber(id)) {
				return activeRecord.byId(Contents.class, id);
			}
			return activeRecord.one(new Take(Contents.class).eq("slug", id));
		}
		return null;
	}

	public Paginator<Contents> getContentsPage(Take take) {
		if (null != take) {
			return activeRecord.page(take);
		}
		return null;
	}

	public Paginator<Contents> getArticles(Take take) {
		return this.getContentsPage(take);
	}

	public Integer publish(Contents contents) {
		if (null == contents)
			throw new TipException("文章对象为空");
		if (StringKit.isBlank(contents.getTitle()))
			throw new TipException("文章标题不能为空");
		if (contents.getTitle().length() > TaleConst.MAX_TITLE_COUNT) {
			throw new TipException("文章标题最多可以输入" + TaleConst.MAX_TITLE_COUNT + "个字符");
		}

		if (StringKit.isBlank(contents.getContent()))
			throw new TipException("文章内容不能为空");
		// 最多可以输入5w个字
		int len = contents.getContent().length();
		if (len > TaleConst.MAX_TEXT_COUNT)
			throw new TipException("文章内容最多可以输入" + TaleConst.MAX_TEXT_COUNT + "个字符");
		if (null == contents.getAuthor_id())
			throw new TipException("请登录后发布文章");

		if (StringKit.isNotBlank(contents.getSlug())) {
			if (contents.getSlug().length() < 5) {
				throw new TipException("路径太短了");
			}
			if (!TaleUtils.isPath(contents.getSlug()))
				throw new TipException("您输入的路径不合法");

			int count = activeRecord
					.count(new Take(Contents.class).eq("type", contents.getType()).eq("slug", contents.getSlug()));
			if (count > 0)
				throw new TipException("该路径已经存在，请重新输入");
		}

		contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

		int time = DateKit.getCurrentUnixTime();
		contents.setCreated(time);
		contents.setModified(time);

		String tags = contents.getTags();
		String categories = contents.getCategories();

		Integer cid = activeRecord.insert(contents);

		metasService.saveMetas(cid, tags, Types.TAG);
		metasService.saveMetas(cid, categories, Types.CATEGORY);

		return cid;
	}

	public void updateArticle(Contents contents) {
		if (null == contents || null == contents.getCid()) {
			throw new TipException("文章对象不能为空");
		}
		if (StringKit.isBlank(contents.getTitle())) {
			throw new TipException("文章标题不能为空");
		}
		if (contents.getTitle().length() > TaleConst.MAX_TITLE_COUNT) {
			throw new TipException("文章标题最多可以输入" + TaleConst.MAX_TITLE_COUNT + "个字符");
		}
		if (StringKit.isBlank(contents.getContent())) {
			throw new TipException("文章内容不能为空");
		}
		if (contents.getContent().length() > TaleConst.MAX_TEXT_COUNT)
			throw new TipException("文章内容最多可以输入" + TaleConst.MAX_TEXT_COUNT + "个字符");
		if (null == contents.getAuthor_id()) {
			throw new TipException("请登录后发布文章");
		}
		int time = DateKit.getCurrentUnixTime();
		contents.setModified(time);

		Integer cid = contents.getCid();

		contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

		activeRecord.update(contents);

		if (!StringKit.equals(contents.getType(), Types.PAGE)) {
			String sql = "delete from t_relationships where cid = ?";
			activeRecord.execute(sql, cid);
		}

		metasService.saveMetas(cid, contents.getTags(), Types.TAG);
		metasService.saveMetas(cid, contents.getCategories(), Types.CATEGORY);
	}

	public void update(Contents contents) {
		if (null != contents && null != contents.getCid()) {
			activeRecord.update(contents);
		}
	}

	public void delete(int cid) {
		Contents contents = this.getContents(cid + "");
		if (null != contents) {
			activeRecord.delete(Contents.class, cid);
			activeRecord.execute("delete from t_relationships where cid = ?", cid);
		}
	}

	public Paginator<Contents> getArticles(Integer mid, int page, int limit) {
		// String countSql = "select count(0) from t_contents a left join
		// t_relationships b on a.cid = b.cid "
		// + "where b.mid = ? and a.status = 'publish' and a.type = 'post' and
		// (a.allow_feed = 1 or a.allow_feed = 0 and a.author_id = )"
		// + uid;
		Integer uid = -1;
		Users loginUser = TaleUtils.getLoginUser();
		if (null != loginUser)
			uid = loginUser.getUid();
		StringBuilder sql = new StringBuilder();
		sql.append("select count(0) from t_contents a left join t_relationships b on a.cid = b.cid")
				.append(" where b.mid = ? and a.status = 'publish' and a.type =	'post'")
				.append(" and (a.allow_feed = 1 or a.allow_feed = 0 and a.author_id = ").append(uid).append(")");

		int total = activeRecord.one(Integer.class, sql.toString(), mid);

		PageRow pageRow = new PageRow(page, limit);
		Paginator<Contents> paginator = new Paginator<>(total, pageRow.getPage(), pageRow.getLimit());
		sql.setLength(0);
		sql.append("select a.* from t_contents a left join t_relationships b on a.cid = b.cid")
				.append(" where b.mid = ? and a.status = 'publish' and a.type = 'post'")
				.append(" and (a.allow_feed = 1 or a.allow_feed = 0 and a.author_id = ").append(uid)
				.append(") order by a.created desc limit ").append(pageRow.getOffSet()).append(",").append(limit);
		// String sql = "select a.* from t_contents a left join t_relationships
		// b on a.cid = b.cid "
		// + "where b.mid = ? and a.status = 'publish' and a.type = 'post' and
		// a.allow_feed = 1 order by a.created desc limit "
		// + pageRow.getOffSet() + "," + limit;

		List<Contents> list = activeRecord.list(Contents.class, sql.toString(), mid);
		if (null != list) {
			paginator.setList(list);
		}
		return paginator;
	}

	public List<Contents> getArticlesList(Take take) {
		return activeRecord.list(take);
	}

}
