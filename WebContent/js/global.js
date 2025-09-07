/*********************全局变量******************/
var wait_id;	//等待框ID
var t_global_current_dialog_id;		//当前打开的对话框id（通用：增删改查）
var jsonp_callback_name="callback";		//jsonp跨域的回调方法名
//
//获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
var curWwwPath = window.document.location.href;
//获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
var pathName = window.document.location.pathname;
var pos = curWwwPath.indexOf(pathName);
//获取主机地址，如： http://localhost:8083
var localhostPaht = curWwwPath.substring(0, pos);
//获取带"/"的项目名，如：/uimcardprj
var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
//项目根目录
var rootPath=localhostPaht+projectName+"/";

/*****加载layui模块*****/
var layer;
layui.use('layer', function() {
	layer = layui.layer;
});
//页面渲染后执行
$(document).ready(function(){
	
});
//"成功"和"错误"提示对话框（isSuccess=是否成功对话框【false=错误框；null=信息框】；title=标题；content=内容；okFn=点“确定”的回调函数）
function resultDialog(isSuccess,title,content,okFn,id){
	var t_icon=isSuccess==true?6:5;
	layer.open({
		title:"<b>"+title+"</b>",
	    content: content,
	    id:id,
	    icon:t_icon,
	    btn: '确定',
	    yes: function(index){
	    	layer.close(index);
	    	if(okFn!=null){okFn();}
	    }
	});
}
//确认框(okFn=确认后回调函数；cencelFn=取消后回调函数）
function confirm(title,content,okFn,cencelFn){
	layer.confirm(content, {
		title:"<b>"+title+"</b>",
		icon:3,
		btn: ['确定','取消'] //按钮
	}, function(index){
		layer.close(index);
		if(okFn!=null){okFn();}
	}, function(){
		if(cencelFn!=null){cencelFn();}
	});
}
//弹出：内容窗口框——title=标题；content=HTML之UI代码
function htmlDialog(title, content, width,height){
	var width_set=(width!=null?width:900)+"px";
	var height_set=(height!=null?height+"px":"");
	return layer.open({
		  type: 1 //Page层类型
		  ,id:id
		  ,skin: 'layui-layer-rim' //加上边框
		  ,area: [width_set,height_set]
		  ,title: "<b>"+title+"</b>"
		  ,shade: 0.6 //遮罩透明度
		  ,maxmin: true //允许全屏最小化
		  ,content: "<div style='padding:10px'>"+content+"</div>"
	});
}
//弹出：内容窗口框——title=标题；返回窗口index索引（类似id）
function openDialog(url,title,success){
	var index = layer.open({
        title : title,
        type : 2,
        content : url,
        maxmin:true,	//允许全屏最小化
        success : function(layerObj, index){
        	if(success!=null){
        		success(layerObj,index);
        	}
        }
    });
    layer.full(index);
	return index;
}
//关闭：asyncbox的HTML内容框，waitTime=等待多少毫秒关闭
function closeDialog(id,waitTime){
	setTimeout(function(){
		layer.close(id);
	}, waitTime!=null?waitTime:10);
}
/*删除所有的标签*/
function delLab(str){
	return str.replace(/<[^>].*?>/g,"");  
}
/*提示框*/
function promptDialog(isSuccess,content){
	var imgName=isSuccess==true?"success_ico.png":"error_ico.png";
	imgName=isSuccess==null?"info_ico.png":imgName;
	content="<table style='margin:0 auto'><tr><td style='padding-right:5px'><img src='"+rootPath+"/image/"+imgName+"' style='margin-bottom:-4px'/></td><td>"+content+"</td></tr></table>";
	layer.msg(content);
}
/*等待框（处理框、加载框）*/
function waitShow(){
	wait_id=layer.load(1, {
		// content: html,
		shade : [ 0.6, '#000' ]
	// 0.1透明度的白色背景
	});
}
/* 关闭等待框（处理框、加载框） */
function waitClose(){
	closeDialog(wait_id);
}
/*通用ajax表单请求（对返回的数据进行验证）——formObj=form表单对象*/
function ajaxForm(formObj,successFn,failureFn,errorFn,isWaitShow,title,content,successInfo,isTipsShow){
	var t_formObj_jq=$(formObj);
	var url=t_formObj_jq.attr("action");
	var jsonData=t_formObj_jq.serialize();
	ajax(url, jsonData, successFn,failureFn,errorFn,isWaitShow,title,content,successInfo,isTipsShow);
}
/**
 * 通用ajax请求（对返回的数据进行验证）
 * @param url : 请求URL
 * @param jsonData : json数据参数（还可以URL参数拼凑法）
 * @param successFn : 成功回调函数；failureFn=失败回调函数；errorFn=异常回调函数
 * @param isWaitShow : 是否显示等待框；title=等待框标题；content=等待框内容
 **/
function ajax(url,jsonData,successFn,failureFn,errorFn,isWaitShow,title,content,successInfo,isTipsShow){
	if(isWaitShow==null || isWaitShow==true){
		waitShow();	//显示等待框
	}
	$.ajax({
		url : url,
		traditional: true,
		data : jsonData,
		type : "post",
		jsonp:jsonp_callback_name,
		dataType:"jsonp",
		xhrFields: {
            withCredentials: true
		},
		success : function(result) {
			ajaxCallbackSuccessProcess(result,successFn,failureFn,errorFn,isWaitShow,title,successInfo,isTipsShow);
		},
		error : function(data) {
			if(isWaitShow==null || isWaitShow==true){
				waitClose();
			}
			if(errorFn!=null){
				errorFn(data);
			}else{
				promptDialog(false,"操作失败，请稍后再试");
			}
		}
	});
}
//ajax请求成功后的后续回调处理
function ajaxCallbackSuccessProcess(result,successFn,failureFn,errorFn,isWaitShow,title,successInfo,isTipsShow){
	if(isWaitShow==null || isWaitShow==true){
		waitClose();
	}
	try{
		title=title || "操作";
		successInfo=successInfo || "操作成功";
		var data=JSON.stringify(result);
		var code=result["code"]; 	//状态
		//成功
		if(code==1){
			if(isTipsShow==null || isTipsShow==true){
				var t_id=promptDialog(true,successInfo);
			}
			if(successFn!=null){
				successFn(result,data);
			}
		}else{
			var message=result["message"];
			if(code!=null){
				switch(code){
					//未登录
					case 6 : {
						resultDialog(false,"未登录",message,function(){
							parent.window.location=result["href"];
						});
						break;
					}
					//用户异常（禁用）
					case 10003 : {
						resultDialog(false,"用户异常",message,function(){
							parent.window.location=result["href"];
						});
						break;
					}
					//未完善信息
					case 10002 : {
						resultDialog(false,"未完善信息",message,function(){
							parent.window.location=result["href"];
						});
						break;
					}
					//没有权限
					case 4 : {
						if(isTipsShow==null || isTipsShow==true){
							promptDialog(false,message);
						}
						break;
					}
				}
			}
			if(failureFn!=null){
				failureFn(result,data);
			}else{
				if(isTipsShow==null || isTipsShow==true){
					promptDialog(false,message);
				}
			}
		}
	}catch(e){
		if(errorFn!=null){
			errorFn(data);
		}else{
			if(isTipsShow==null || isTipsShow==true){
				promptDialog(false,"系统或网络故障，请稍后再试");
			}
		}
	}
}
//是否其他设备（移动端）
function isMobileDevice() {
	return navigator.userAgent.match(/(iPhone|iPod|Android|ios)/i);
}
//删除左右2端的空格
function trim(str){if(str==null){return "";}else{return str.replace(/(^\s*)|(\s*$)/g, "");}}
//模拟调用——元素点击
function callClick(id){$("#"+id).click();}

//重置/初始化当前对象所属的表单（fn=重置成功后执行的回调方法）
function formReset(obj,fn){
	var t_form_obj=obj.form;
	if(t_form_obj!=null){
		confirm("重置/初始化","您确定要【重置/初始化】当前表单元素吗？",function(){
			t_form_obj.reset();
			if(fn!=null){
				fn(obj);
			}
		});
	}else{
		resultDialog(false,"重置/初始化","当前控件元素没有所属的表单");
	}
}
//获取表单参数Json格式
function getFormParamJson(formObj){
	var t_form_jq=$(formObj);
	var t_param_json = {};
    var t_param_arr= t_form_jq.serializeArray();
    $.each(t_param_arr, function() {
    	t_param_json[this.name] = this.value;
    });
    return t_param_json;
}
//获取当前URL中指定的参数
function getUrlParam(variable){
	var query = window.location.search.substring(1);
	var vars = query.split("&");
	for (var i = 0; i < vars.length; i++) {
		var pair = vars[i].split("=");
		if (pair[0] == variable) {
			return pair[1];
		}
	}
	return null;
}