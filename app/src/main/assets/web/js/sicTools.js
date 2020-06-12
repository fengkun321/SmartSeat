/**响应码*/
var responseCode = {
    OK : {
        code : "OK",
        message : "处理完成"
    },
    SESSION_TIME_OUT : {
        code : "SESSION_TIME_OUT",
        message : "用户未登录"
    },
    CONNECT_TIME_OUT : {
        code : "CONNECT_TIME_OUT",
        message : "网络已超时"
    },
    PARAMETER_ERROR : {
        code : "PARAMETER_ERROR",
        message : "参数错误"
    },NOT_FIND_DATA :{
        code : "NOT_FIND_DATA",
        message : "找不到相关数据"
    }

};

/**AJAX工具方法封装*/

/**
 * ajax方法get请求封装
 *
 * @url 请求地址
 * @params 请求参数
 * @callback 回调函数
 * @isShowLoading 是否显示Loading
 **/
function ajax_getServer(url, params, callback, isShowLoading) {
    ajax_requestServer(url, params, callback, "get",true, isShowLoading);
}

/**
 * ajax方法post请求封装
 *
 * @url 请求地址
 * @params 请求参数
 * @callback 回调函数
 * @isShowLoading 是否显示Loading
 **/
function ajax_postServer(url, params, callback, isShowLoading) {
    ajax_requestServer(url, params, callback, "post",true,isShowLoading);
}

/**
 * 同步 ajax方法get请求封装
 *
 * @url 请求地址
 * @params 请求参数
 * @callback 回调函数
 * @isShowLoading 是否显示Loading
 **/
function ajax_getServerSync(url, params, callback, isShowLoading) {
    ajax_requestServer(url, params, callback, "get",false, isShowLoading);
}

/**
 * 同步 ajax方法post请求封装
 *
 * @url 请求地址
 * @params 请求参数
 * @callback 回调函数
 * @isShowLoading 是否显示Loading
 **/
function ajax_postServerSync(url, params, callback, isShowLoading) {
    ajax_requestServer(url, params, callback, "post",false, isShowLoading);
}

/**
 * ajax方法请求封装
 *
 * @url 请求地址
 * @params 请求参数
 * @callback 回调函数
 * @type 请求方式,get,post等
 * @isAsync 是否异步
 * @isShowLoading 是否显示Loading
 **/
function ajax_requestServer(url, params, callback, type, isAsync,isShowLoading) {
    if (params == undefined)
        params = {};
    if (type == undefined)
        type = "post";
    if (isAsync == undefined)
        isAsync = true;
    if (isShowLoading == undefined)
        isShowLoading = false;

    if(isShowLoading){
        showLoadingDialog();
    }

    $.ajax({
        url: url,
        type: type,
        async: isAsync,//是否异步
        crossDomain: true,
        dataType: "json",
        data: params,
        success: function (res) {
            if(isShowLoading) {
                hideLoadingDialog();
            }
            if (callback != undefined)
                callback(res, url, params);
        }, error: function (XMLHttpRequest, textStatus, errorThrown) {
            if(isShowLoading) {
                hideLoadingDialog();
            }
        }

    });
}

/**展示加载*/
function showLoadingDialog(title){
    title = typeof(title) == "undefined" ? "加载中" : title;
    return Dialog.init('<div class="loading">'+title+'</div>',{
        maskClick : false,
        index:3684
    });
}

/**隐藏加载*/
function hideLoadingDialog(){
    Dialog.close(3684);
}

/**消息提示弹窗*/
function alertDialog(msg,onButton1){
    Dialog.init('<p style="text-align: center;color:#666">'+msg+'</p>',{
        maskClick : false,
        button:{
            "确定":function(){
                Dialog.close(this);
                if(typeof(onButton1) != "undefined"){
                    onButton1();
                }
            }
        }
    });
}

/**确认框*/
function confirmDialog(msg,onButton1,onButton2,title){
    title = typeof(title) != "undefined" ? '<p style="text-align: center;color:#666">'+title+'</p>' : '';

    Dialog.init('<p style="text-align: center;color:#666">'+msg+'</p>',{
        title : title,
        maskClick : false,
        button:{
            "取消":function(){
                Dialog.close(this);
                if(typeof(onButton2) != "undefined"){
                    onButton2();
                }
            },
            "确定":function(){
                Dialog.close(this);
                if(typeof(onButton1) != "undefined"){
                    onButton1();
                }
            }
        }
    })
}

/**定时消失信息窗口*/
function messageDialog(msg,time){
    time =  typeof(time) != "undefined" ? time : 1000;
    Dialog.init(msg,time);
}

/**输入信息弹窗*/
function inputTextDialog(textHandler,title,placeholder,maxLength){
    title = typeof(title) == "string" ? title : "提示";
    placeholder = typeof(placeholder) == "string" ? placeholder : "请输入";
    maxLength = typeof(maxLength) == "number" ? maxLength : "";

    Dialog.init('<input type="text" placeholder="'+placeholder+'" maxlength="' +maxLength+ '" style="margin:8px 0;width:100%;padding:11px 8px;font-size:15px; border:1px solid #999;"/>',{
        title : '<p style="text-align: center;color:#666">'+title+'</p>',
        maskClick : true,
        button : {
            "取消" : function(){
                Dialog.close(this);
            },
            "确定" : function(){
                if(this.querySelector('input').value.length >0){
                    var text = this.querySelector('input').value;
                    text = text.trim();
                    if(text.length > 0){
                        Dialog.close(this);
                        if(typeof(textHandler) != "undefined"){
                            textHandler(text);
                        }
                    }else{
                        this.querySelector('input').value = '';
                    }

                }
            }
        }
    });
}
