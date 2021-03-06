var tale = new $.tale();
$(function() {
	var bookListTable = new BookListTable();
	bookListTable.init();
	
	$("#addBookBtn").click(function() {
		tale.clearForm("bookForm");
		$("#bookModal").modal();
	});
	$("#updateBookBtn").click(function() {
		var selectList = bookListTable.getSelections();
		var len = selectList.length;
		if (len == 0 || len > 1) {
			tale.alertWarn({
				text : '请选择一条数据',
			});
			return;
		}
		$("#bookModal").modal();
		var data = selectList[0];
		tale.autoFillForm('bookForm', data);
		if (data.begin_time)
			$("#beginTime").val(new Date(data.begin_time * 1000).Format('yyyy-MM-dd HH:mm:ss'));
		if (data.end_time)
			$("#endTime").val(new Date(data.end_time * 1000).Format('yyyy-MM-dd HH:mm:ss'));
	});
	$("#deleteBookBtn").click(function() {
		var selectList = bookListTable.getSelections();
		var len = selectList.length;
		if (len == 0) {
			tale.alertWarn({
				text : '请至少选择一条数据',
			});
			return;
		}
		var params = [];
		$.each(selectList, function(i, select) {
			params.push(select.id);
		})
		tale.post({
			url : '/admin/book/deleteBook',
			data : {ids : params.join(",")},
			success : function(result) {
				if (!result.success) {
					tale.alertError(result.msg);
					return;
				}
				bookListTable.refresh();
			}
		});
	});
	$("#saveBtn").click(function() {
		var params = tale.getParams("bookForm");
		tale.post({
			url : '/admin/book/saveBook',
			data : params,
			success : function(result) {
				if (!result.success) {
					tale.alertError(result.msg);
					return;
				}
				bookListTable.refresh();
			}
		});
	});
	
});
var BookListTable = function() {
	var table = new Object();
	table.init = function() {
		$("#bookListTable").bootstrapTable({
			url : '/admin/book/selectBookList', 
			method : 'post', 
			dataType : 'json',
			striped : true, // 是否显示行间隔色
			pagination : true, 
			queryParams : table.queryParams,
			pageNumber : 1, 
			pageSize : 10, 
			pageList : [ 10, 20, 50], 
			strictSearch : true,
			search : true,
			showRefresh : true,
			clickToSelect : true, // 是否启用点击选中行
			idField : "id", // 每一行的唯一标识，一般为主键列
			columns : [ {
				field : 'id', visible : false,
			}, {
				title : '序号', checkbox : true
			}, {
				field : 'name', title : '书单名', width : '25%', 
				formatter : function(value, row, index) {
					return '<a target="#">'+value+'</a>';
				}
			}, {
				field : 'begin_time', title : '开始时间', width : '25%',
				formatter : function(value, row, index) {
					return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
				}
			}, {
				field : 'end_time', title : '结束时间', width : '25%',
				formatter : function(value, row, index) {
					if (value)
						return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
					return '';
				}
			}, {
				field : 'status', title : '状态',
				formatter : function(value, row, index) {
					if (value != '已读')
						return '<span class="label label-success">'+value+'</span>';
					return '<span class="label label-default">'+value+'</span>';
				}
			}, {
				field : 'oper', title : '书摘', width : '10%',
				formatter : function(value, row, index) {
					var oper = [];
					oper.push('<a class="btn btn-warning btn-sm waves-effect waves-light m-b-5" href="/books/'+row.id+'" target="_blank"><i class="fa fa-rocket"></i> <span>查看</span></a>');
	                return oper.join(" ");     
				}
			}, ],
			toolbar : '#operateToolbar',
		});
	};
	
	table.loadTable = function(data) {
		$("#bookListTable").bootstrapTable('load', data);
	} 
	table.refresh = function() {
		$("#bookListTable").bootstrapTable('refresh');
	}
	table.getSelections = function() {
		return $("#bookListTable").bootstrapTable('getSelections');
	}
	table.queryParams = function (params) {
		return {};
	}
	return table;
}
