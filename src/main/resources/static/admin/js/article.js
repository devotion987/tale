var mditor, htmlEditor;
var tale = new $.tale();
var attach_url = $('#attach_url').val();
// 每60秒自动保存一次草稿
var refreshIntervalId = setInterval("autoSave()", 60 * 1000);

$(document).ready(function () {

	// window.mditor =
    mditor = window.mditor = Mditor.fromTextarea(document.getElementById('md-editor'));
    
    mditor.on('ready', function() {
    	var upload = function(file) {
    		if (null == file)
    			return;
    		var name = file.name || 'screenshot.png';
    		name = name.replace(/\.(?:jpg|gif|png)$/i, ''); // clear ext
	        name = name.replace(/\W+/g, '_'); // clear unvalid chars
	        file.name = name;
	        var data = new FormData();
	        data.append('fileToUpload', file);
	        $.ajax('/admin/article/uploadToQiniu', {
	        	data : data,
	        	type : 'POST',
	        	processData : false,
	        	contentType : false
	        })
	        .done (function(res) {
	        	mditor.editor.insertBeforeText('![alt](' + res + ")\n");
	        })
	        .fail(function ($xhr) {
                if ($xhr.responseText) {
                    $.alert($xhr.responseText);
                }
            });
    	}
    	
    	//示例，更改「图片」按钮配置，其它按钮是同样的方法
    	var imgBtn = mditor.toolbar.getItem('image');
    	//替换按钮动作
    	imgBtn.handler = function(){
    		//自定义处理逻辑 this 指向当前 mditor 实例
    		// this.editor.wrapSelectText('![alt](', ')');
    		var accept = {
				image : 'image/png,image.gif,image/jpg,image/jpeg'
    		};
    		var $file = $('<input type="file" accept="' + accept.image + '">');
    		$file.click();
    		$file.on('change', function() {
    			var file = this.files[0];
    			upload(file);
    		});
    	};
    	
    	// 粘贴上传
    	mditor.editor.on('paste', function(evt) {
    		var data = clipboardData;
    		if (typeof data !== 'object')
    			return;
    		return $.each(data.items, function(i, item) {
    			if (item.kind === 'file' && item.type.indexOf('image/') !== -1) {
    				var blob = file.getAsFile();
    				upload(blob);
    			}
    		});
    	});
    	// 拖拽上传
    	mditor.editor.on('drop', function(evt) {
    		evt.preventDefault();
    		return $.each(evt.dataTransfer.files, function(i, file) {
    			return upload(file);
    		});
    	});
    });
	
    $('.toggle').toggles({
        on: true,
        text: {
            on: '开启',
            off: '关闭'
        }
    });

    $(".select2").select2({
        width: '100%'
    });
    
    $(".multiple-selTag").select2({
    	width: '100%',
    	tags: true,
    	tokenSeparators: [',', ' ']
    });

    $('div.allow-false').toggles({
        off: true,
        text: {
            on: '开启',
            off: '关闭'
        }
    });

    if($('#thumb-toggle').attr('thumb_url') != ''){
        $('#thumb-toggle').toggles({
            on: true,
            text: {
                on: '开启',
                off: '关闭'
            }
        });
        $('#thumb-toggle').attr('on', 'true');
        $('#dropzone').css('background-image', 'url('+ $('#thumb-container').attr('thumb_url') +')');
        $('#dropzone').css('background-size', 'cover');
        $('#dropzone-container').show();
    } else {
        $('#thumb-toggle').toggles({
            off: true,
            text: {
                on: '开启',
                off: '关闭'
            }
        });
        $('#thumb-toggle').attr('on', 'false');
        $('#dropzone-container').hide();
    }

    Dropzone.autoDiscover = false;

    var thumbdropzone = $('.dropzone');

    // 缩略图上传
    $("#dropzone").dropzone({
        url: "/admin/attach/upload",
        filesizeBase:1024,//定义字节算法 默认1000
        maxFilesize: '10', //MB
        fallback:function(){
            tale.alertError('暂不支持您的浏览器上传!');
        },
        acceptedFiles: 'image/*',
        dictFileTooBig:'您的文件超过10MB!',
        dictInvalidInputType:'不支持您上传的类型',
        init: function() {
            this.on('success', function (files, result) {
                console.log("upload success..");
                console.log(" result => " + result);
                if(result && result.success){
                    var url = attach_url + result.payload[0].fkey;
                    console.log('url => ' + url);
                    thumbdropzone.css('background-image', 'url('+ url +')');
                    thumbdropzone.css('background-size', 'cover');
                    $('.dz-image').hide();
                    $('#thumb_img').val(url);
                }
            });
            this.on('error', function (a, errorMessage, result) {
                if(!result.success && result.msg){
                    tale.alertError(result.msg || '缩略图上传失败');
                }
            });
        }
    });

});
/*
 * 自动保存为草稿
 * */
function autoSave() {
    var content = $('#fmt_type').val() == 'markdown' ? mditor.value : htmlEditor.summernote('code');
    var title = $('#articleForm input[name=title]').val();
    if (title != '' && content != '') {
        $('#content-editor').val(content);
        $("#articleForm #status").val('draft');
        var params = $("#articleForm").serialize();
        var url = $('#articleForm #cid').val() != '' ? '/admin/article/modify' : '/admin/article/publish';
        tale.post({
            url: url,
            data: params,
            success: function (result) {
                if (result && result.success) {
                    $('#articleForm #cid').val(result.payload);
                } else {
                    tale.alertError(result.msg || '保存文章失败');
                }
            }
        });
    }
}

/**
 * 保存文章
 * @param status
 */
function subArticle(status) {
    var content = $('#fmt_type').val() == 'markdown' ? mditor.value : htmlEditor.summernote('code');
    var title = $('#articleForm input[name=title]').val();
    if (title == '') {
        tale.alertWarn('请输入文章标题');
        return;
    }
    if (content == '') {
        tale.alertWarn('请输入文章内容');
        return;
    }
    clearInterval(refreshIntervalId);
    $('#content-editor').val(content);
    $("#articleForm #status").val(status);
    $("#articleForm #tags").val($('#multiple-selTag').val());
    $("#articleForm #book_id").val($('#multiple-selBook').val());
    var params = $("#articleForm").serialize();
    var url = $('#articleForm #cid').val() != '' ? '/admin/article/modify' : '/admin/article/publish';
    tale.post({
        url: url,
        data: params,
        success: function (result) {
            if (result && result.success) {
                tale.alertOk({
                    text: '文章保存成功',
                    then: function () {
                        setTimeout(function () {
                            window.location.href = '/admin/article';
                        }, 500);
                    }
                });
            } else {
                tale.alertError(result.msg || '保存文章失败');
            }
        }
    });
}

function allow_comment(obj) {
    var this_ = $(obj);
    var on = this_.attr('on');
    if (on == 'true') {
        this_.attr('on', 'false');
        $('#allow_comment').val('false');
    } else {
        this_.attr('on', 'true');
        $('#allow_comment').val('true');
    }
}

function allow_feed(obj) {
    var this_ = $(obj);
    var on = this_.attr('on');
    if (on == 'true') {
        this_.attr('on', 'false');
        $('#allow_feed').val('false');
    } else {
        this_.attr('on', 'true');
        $('#allow_feed').val('true');
    }
}

function add_thumbimg(obj) {
    var this_ = $(obj);
    var on = this_.attr('on');
    console.log(on);
    if (on == 'true') {
        this_.attr('on', 'false');
        $('#dropzone-container').addClass('hide');
        $('#thumb_img').val('');
    } else {
        this_.attr('on', 'true');
        $('#dropzone-container').removeClass('hide');
        $('#dropzone-container').show();
    }
}