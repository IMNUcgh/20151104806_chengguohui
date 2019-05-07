<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link href="/20151104806_chengguohui_cloudStorage/css/file.css" rel='stylesheet' type='text/css'/>
	<link href="/20151104806_chengguohui_cloudStorage/css/buttons.css" rel='stylesheet' type='text/css'/>
	<script type="text/javascript" src="/20151104806_chengguohui_cloudStorage/js/jquery-1.4.4.min.js"></script>
	<title>文件目录</title>
</head>
<body style="background:#fff url(/20151104806_chengguohui_cloudStorage/images/scenery2.jpg) no-repeat left top;background-size:100%;">
	<script>
		var sysMsg = "${sysMsg}";
		if(sysMsg != "")
			alert(sysMsg);
		
		function downloads(){
			var idsstr='';
			var foldersstr='';
			$("input[type=checkbox]").each(function(){  
				if($(this).attr("checked")){
					idsstr += $(this).attr('id')+","; 
					foldersstr += $(this).attr('name')+","; 
				} 
				});
			
			window.location.href="downloads?file="+idsstr+"&folder="+foldersstr;
		}
		
		function moveFile(fileId){
			var movedFolder = $('#movedFolder_'+fileId).val();
			if(movedFolder==''){
				alert("请输入目标文件夹！");
				return;
			}		
			
			window.location.href="move?file="+fileId+"&folderName="+movedFolder+"&path="+$('#pathId').val();
			
		}
	</script>
	
<div id="fileForm">
		<div id="upload">
			<br/>
			<h2>文件上传</h2>
			<form class="form-control" action="upload" method="post" enctype="multipart/form-data" accept-charset="utf-8" >
				选择文件：<input type="file" name="dataList" multiple="multiple" /><br/>
				<input type="hidden" name="path" id="pathId" value="${requestScope.folderId}" />
				<input type="submit" value="          上传           " class="button "/>
			</form>
		</div>
		<div id="folder">
			<br/>
			<h2>新建文件夹</h2>
			<form class="form-control" action="folderAdd" method="POST" accept-charset="utf-8" >
				文件夹名：<input type="text" name="folderName"/><br/>
				<input type="hidden" name="path" value="${requestScope.folderId}" />
				<input type="submit" value="          新建           " class="button "/>
			</form>
		</div>
	</div>
	
	<table style="margin: 0px;">
		<tr>
			<th></th><th></th>
			<th>文件名</th>
			<th>文件类型</th>
			<th>大小</th>
			<th>上传时间</th>
			<th>分享</th>
			<th>外链分享</th>
			<th>删除</th>
			<th>备份</th>
			<th>移动</th>
		</tr>
		<c:if test="${requestScope.fatherFolder.id != null }">
			<tr>
			<td></td>
				<td>
					<a href="/20151104806_chengguohui_cloudStorage/main/file?dir=${requestScope.fatherFolder.id }"><img src="/20151104806_chengguohui_cloudStorage/images/folder.png" alt="folder" /></a>
				</td>
				<td>上级文件夹</td>
				<td>文件夹</td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
			</tr>
		</c:if>
		<c:forEach var="list" items="${requestScope.folderList }">
			<tr>
			<td></td>
				<td><a href="/20151104806_chengguohui_cloudStorage/main/file?dir=${list.id}"><img src="/20151104806_chengguohui_cloudStorage/images/folder.png" alt="folder" /></a></td>
				<td>${list.name }</td>
				<td>文件夹</td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td>
					<a href="/20151104806_chengguohui_cloudStorage/main/deleteDir?folder=${list.id}&fatherFolder=${requestScope.fatherFolder.id}">
						<img src="/20151104806_chengguohui_cloudStorage/images/delete.png" alt="delete" />
					</a>
				</td>
				<td></td><td></td>
			<tr>
		</c:forEach>
		<c:forEach var="list" items="${requestScope.fileList }">
			<tr>
			<td><input type="checkbox" id="${list.id }" name="${requestScope.folderId }"/></td>
				<td><a href="download?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/download.png" alt="download" /></a></td>
				<td>${list.name}</td>
				<td>${list.fileType }</td>
				<td>${list.size } KB</td>
				<td>${list.uploadTime }</td>
				<c:if test="${list.status == 0}">
					<td>
						<a href="setAccess?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/private.png" alt="status" /></a>
					</td>
				</c:if>
				<c:if test="${list.status == 2}">
					<td>
						<a href="setAccess?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/private.png" alt="status" /></a>
					</td>
				</c:if>
				<c:if test="${list.status == 1}">
					<td>
						<a href="setAccess?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/public.png" alt="status" /></a>
					</td>
				</c:if>
				<td><a href="outShare?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/share.png" alt="share" width="32px" height="32px"/></a></td>
				<td><a href="delete?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/delete.png" alt="delete" width="32px" height="32px"/></a></td>
				<td><a href="copy?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/copy.png" alt="copy"  width="32px" height="32px"/></a></td>
				<td>
				<input type="text" id="movedFolder_${list.id }" style="
    width: 100px;
"/>
				<a href="#" onclick="moveFile('${list.id}')"><img src="/20151104806_chengguohui_cloudStorage/images/move.png" alt="move"  width="32px" height="32px"/></a></td>
			</tr>
		</c:forEach>
		
		<div id="multiDownload">
			<form class="form-control" action="" method="post"  accept-charset="utf-8" >
				<input type="button" value="       多文件下载           " class="button " onclick="downloads();"/>
			</form>
		</div>
	</table>
	
</body>
</html>