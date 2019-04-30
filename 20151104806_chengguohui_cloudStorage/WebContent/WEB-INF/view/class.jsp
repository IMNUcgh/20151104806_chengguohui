<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link href="/20151104806_chengguohui_cloudStorage/css/file.css" rel='stylesheet' type='text/css'/>
	<title>照片</title>
	<script type="text/javascript">
	function previewJpg(name){
		window.open('/20151104806_chengguohui_cloudStorage/upload/'+name);
	}
	</script>
</head>
<body>
	<table style="
    margin: 0px;
">
		<tr>
			<th></th>
			<th>文件名</th>
			<th>文件类型</th>
			<th>大小</th>
			<th>上传时间</th>
			<th>删除</th>
		</tr>
		<c:forEach var="list" items="${requestScope.fileList }">
			<tr>
				<td><a href="download?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/download.png" alt="download" /></a>
				<c:if test="${list.fileType == 'jpg'}">
					<a href="#" onclick="previewJpg('${list.name}')">预览</a>
				</c:if>
				<c:if test="${list.fileType == 'mp3'}">
					<audio src="/20151104806_chengguohui_cloudStorage/upload/${list.name }" controls="controls">
Your browser does not support the audio element.
</audio>
				</c:if>
				
				<c:if test="${list.fileType == 'mp4'}">
					<video  controls="controls" autoplay="autoplay">
					<source  src="/20151104806_chengguohui_cloudStorage/upload/${list.name }" />
Your browser does not support the audio element.
</video >

				</c:if>
				</td>
				<td>${list.name}</td>
				<td>${list.fileType }</td>
				<td>${list.size } KB</td>
				<td>${list.uploadTime }</td>
				<td><a href="delete?file=${list.id }&folder=${requestScope.folderId}"><img src="/20151104806_chengguohui_cloudStorage/images/delete.png" alt="delete" /></a></td>
			</tr>
		</c:forEach>
	</table>
</body>
</html>