package com.tale.controller.admin;

import java.util.List;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.mvc.annotation.Controller;
import com.blade.mvc.annotation.EntityObj;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.view.RestResponse;
import com.tale.controller.BaseController;
import com.tale.model.Finance;
import com.tale.model.Users;
import com.tale.service.FinanceService;

/**
 * 财务管理
 * 
 * @author wugy 2017-5-12 07:29:50
 */
@Controller("admin/finance")
public class FinanceController extends BaseController {

	@Inject
	private FinanceService financeService;

	@Route(value = "", method = HttpMethod.GET)
	public String index() {
		return "admin/financeList";
	}

	@Route(value = "/saveFinance", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> saveFinance(@EntityObj Finance finance) {
		try {
			Users user = user();
			if (null != user) {
				finance.setUid(user.getUid());
			}
			financeService.saveFinance(finance);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("保存失败:" + e);
			return RestResponse.fail("保存失败");
		}
		return RestResponse.ok();
	}

	@Route(value = "/deleteFinance", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> deleteFinance(@QueryParam String ids) {
		try {
			financeService.deleteFinance(ids);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("删除失败:" + e);
			return RestResponse.fail("删除失败");
		}
		return RestResponse.ok();
	}

	@Route(value = "/selectFinanceList", method = HttpMethod.POST)
	@JSON
	public List<Finance> selectFinanceList(@QueryParam(value = "page", defaultValue = "1") int page,
			@QueryParam(value = "limit", defaultValue = "10") int limit, Request request) {
		Take take = new Take(Finance.class).page(page, limit).desc("expense_time");
		List<Finance> financeList = financeService.selectFinanceList(take);
		return financeList;
	}

	@Route(value = "/statistics", method = HttpMethod.GET)
	public String statistics() {
//		String[] dates = {};
//		Finance finance = null;
//		for (String d : dates) {
//			Date date = DateKit.dateFormat(d);
//			for (int i = 1; i <= 30; i++) {
//				finance = new Finance();
//				finance.setMoney(1 + "");
//				finance.setRemark("测试" + i);
//				if (i == 1) {
//					finance.setExpense_time(DateKit.getUnixTimeByDate(date));
//				} else {
//					finance.setExpense_time(DateKit.getUnixTimeByDate(DateKit.dateAdd(DateKit.INTERVAL_DAY, date, i)));
//				}
//				if (i <= 5)
//					finance.setType(Constant.shopping.getDesc());
//				else if (i > 5 && i <= 10)
//					finance.setType(Constant.breakfast.getDesc());
//				else if (i > 10 && i <= 15)
//					finance.setType(Constant.lunch.getDesc());
//				else if (i > 15 && i <= 20)
//					finance.setType(Constant.dinner.getDesc());
//				else if (i > 20 && i <= 25)
//					finance.setType(Constant.traffic.getDesc());
//				else
//					finance.setType(Constant.donation.getDesc());
//				financeService.saveFinance(finance);
//			}
//		}
		return "admin/financeStatistics";
	}

}
