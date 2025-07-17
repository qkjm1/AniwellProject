<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<!DOCTYPE html>
<html>
<head>
<title>알림</title>
<script>
	// ✅ msg 출력
	let msg = '${msg}';
	if (msg.trim().length > 0) {
		alert(msg);
	}

	// ✅ historyBack 여부에 따라 동작 분기
	<c:choose>
	<c:when test="${historyBack}">
	history.back();
	</c:when>
	<c:otherwise>
	location.replace("/");
	</c:otherwise>
	</c:choose>
</script>

</head>

<body></body>
</html>
