<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link href="/20151104806_chengguohui_cloudStorage/css/file.css" rel='stylesheet' type='text/css'/>
	<title>文件分享区</title>
</head>
<body style="background:#fff url(/20151104806_chengguohui_cloudStorage/images/scenery5.jpg) no-repeat left top;background-size:100%;">
	<table>
		<tr>
			<th>操作</th>
			<th>文件名</th>
			<th>文件类型</th>
			<th>大小</th>
			<th>上传时间</th>
			<th>上传用户</th>
		</tr>
		<c:forEach var="list" items="${requestScope.folderList}">
			<tr>
				<td>
					<a href="restore?folder=${list.id}&fileId=-1">恢复</a>&nbsp;
				    <a href="confirmDirDeletion?folder=${list.id}&fileId=-1">清空</a>
				</td>
				<td>${list.name}</td>
				<td>文件夹</td>
				<td></td>
				<td></td>
				<td>${list.createUser.username}</td>
			<tr>
		</c:forEach>
		<c:forEach var="list" items="${requestScope.fileList}">
			<tr>
				<td>
					<a href="restore?fileId=${list.id }&folder=${list.fatherFolder.id}">恢复</a>&nbsp;
				    <a href="deletetrue?fileId=${list.id }&folder=${list.fatherFolder.id}">清空</a>
				</td>
				<td>${list.name }</td>
				<td>${list.fileType }</td>
				<td>${list.size } KB</td>
				<td>${list.uploadTime }</td>
				<td>${list.uploadUser.username }</td>
			</tr>
		</c:forEach>
	</table>
</body>
</html>