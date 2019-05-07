<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head id="Head1">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>口袋云盘</title>
    <link href="css/default.css" rel="stylesheet" type="text/css" />
    <link rel="stylesheet" type="text/css" href="js/themes/default/easyui.css" />
    <link rel="stylesheet" type="text/css" href="js/themes/icon.css" />
    <script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>
    <script type="text/javascript" src="js/jquery.easyui.min.1.2.2.js"></script>
	<script type="text/javascript" src='js/outlook2.js'> </script>
    <script type="text/javascript">
	 var _menus = {"menus":[
						{"menuid":"1","icon":"icon-sys","menuname":"我的文件",
							"menus":[
									{"menuid":"12","menuname":"视频","icon":"icon-log","url":"/20151104806_chengguohui_cloudStorage/main/class?type=movie"},
									{"menuid":"13","menuname":"音乐","icon":"icon-log","url":"/20151104806_chengguohui_cloudStorage/main/class?type=music"},
									{"menuid":"14","menuname":"照片","icon":"icon-log","url":"/20151104806_chengguohui_cloudStorage/main/class?type=photo"},
									{"menuid":"15","menuname":"文档","icon":"icon-log","url":"/20151104806_chengguohui_cloudStorage/main/class?type=document"},
									{"menuid":"16","menuname":"压缩包","icon":"icon-log","url":"/20151104806_chengguohui_cloudStorage/main/class?type=zip"},
									{"menuid":"17","menuname":"文件管理","icon":"icon-log","url":"/20151104806_chengguohui_cloudStorage/main/file?dir=0"}
								]
						},{"menuid":"8","icon":"icon-sys","menuname":"资源共享",
							"menus":[{"menuid":"21","menuname":"共享区","icon":"icon-nav","url":"/20151104806_chengguohui_cloudStorage/main/public"}
								]
						},{"menuid":"9","icon":"icon-sys","menuname":"回收站",
							"menus":[{"menuid":"21","menuname":"回收站","icon":"icon-nav","url":"/20151104806_chengguohui_cloudStorage/main/recycleBin"}
							]
					}
				]};

        $(function() {
            
            $('#loginOut').click(function() {
                $.messager.confirm('系统提示', '您确定要退出本次登录吗?', function(r) {

                    if (r) {
                        location.href = 'index.jsp';
                    }
                });
            })
        });	


    </script>
</head>
<body class="easyui-layout" style="overflow-y: hidden"  scroll="no">
	<noscript>
		<div style=" position:absolute; z-index:100000; height:2046px;top:0px;left:0px; width:100%; background:white; text-align:center;">
    		<img src="images/noscript.gif" alt='抱歉，请开启脚本支持！' />
		</div>
	</noscript>
    <div region="north" split="true" border="false" style="overflow: hidden; height: 30px;
        background: url(images/layout-browser-hd-bg.gif) #7f99be repeat-x center 50%;
        line-height: 20px;color: #fff; font-family: Verdana, 微软雅黑,黑体">
        <span style="float:right; padding-right:20px;" class="head">欢迎 ${sessionScope.user.username } <a id="loginOut">安全退出</a></span>
        <span style="padding-left:10px; font-size: 16px; "><img src="images/headerLogo.png" width="30" height="20" align="absmiddle" />口袋云盘</span>
    </div>
    <div region="south" split="true" style="height: 30px; background: #D2E0F2; ">
        <div class="footer"></div>
    </div>
    <div region="west" hide="true" split="true" title="导航菜单" style="width:180px;" id="west">
		<div id="nav" class="easyui-accordion" fit="true" border="false"></div>
    </div>
    <div id="mainPanle" region="center" style="background: #eee; overflow-y:hidden">
        <div id="tabs" class="easyui-tabs"  fit="true" border="false" >
			<div title="欢迎使用" style="padding:20px;overflow:hidden;color:red;background:#fff url(/20151104806_chengguohui_cloudStorage/images/scenery6.jpg) no-repeat left top;background-size:100%;" >
			</div>
		</div>
    </div>
    
	<div id="mm" class="easyui-menu" style="width:150px;">
		<div id="mm-tabupdate">刷新</div>
		<div class="menu-sep"></div>
		<div id="mm-tabclose">关闭</div>
		<div id="mm-tabcloseall">全部关闭</div>
		<div id="mm-tabcloseother">除此之外全部关闭</div>
		<div class="menu-sep"></div>
		<div id="mm-tabcloseright">当前页右侧全部关闭</div>
		<div id="mm-tabcloseleft">当前页左侧全部关闭</div>
		<div class="menu-sep"></div>
		<div id="mm-exit">退出</div>
	</div>

</body>
</html>