<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<html>
<head>
<title>${crew.title}-크루전용카페</title>
<style>
.container {
	width: 80%;
	margin: 30px auto;
}

.header {
	text-align: center;
	margin-bottom: 30px;
}

.menu {
	display: flex;
	justify-content: center;
	gap: 20px;
	margin-bottom: 20px;
}

.menu a {
	text-decoration: none;
	font-weight: bold;
	color: #333;
}

.content-box {
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
	background-color: #f9f9f9;
	margin-bottom: 20px;
}

.section-title {
	font-size: 1.2em;
	font-weight: bold;
	margin-bottom: 10px;
	border-bottom: 1px solid #ccc;
	padding-bottom: 5px;
	position: relative;
}

.section-title a.write-button {
	position: absolute;
	right: 0;
	font-size: 0.85em;
	text-decoration: none;
	color: #007bff;
}

.section-title a.write-button:hover {
	text-decoration: underline;
}

ul.article-preview {
	list-style: none;
	padding: 0;
}

ul.article-preview li {
	margin: 6px 0;
}

ul.article-preview img {
	width: 100px;
	height: auto;
	vertical-align: middle;
	border-radius: 6px;
	margin-right: 10px;
}

.calendar-box {
	margin-top: 30px;
}
</style>
</head>
<body>

	<p>📌 현재 접속한 crewId: ${crew.id}</p>

	<div class="container">
		<div class="header">
			<h1>🏠 [${crew.title}] 전용 크루 공간</h1>
			<p class="subtitle">📌 소개: ${crew.description}</p>
		</div>

		<div class="menu">
			<a href="/usr/crewCafe/cafeHome?crewId=${crew.id}">🏠 홈</a>
			<a href="/usr/article/list?crewId=${crew.id}&boardId=1">📢 공지사항</a>
			<a href="/usr/article/list?crewId=${crew.id}&boardId=3">📝 자유게시판</a>
			<a href="/usr/article/list?crewId=${crew.id}&boardId=4">📸 사진첩</a>
			<a href="/usr/article/schedule?crewId=${crew.id}">📅 일정</a>
			<c:if test="${crew != null and crew.leaderId == rq.loginedMemberId}">
				<a href="/usr/walkCrewMember/requestList?crewId=${crew.id}">👥 크루 신청자 리스트</a>
			</c:if>
		</div>

		<!-- ✅ 공지사항 섹션 -->
		<div class="content-box">
			<div class="section-title">
				📢 최근 공지사항
				<a class="write-button" href="/usr/article/write?crewId=${crew.id}&boardId=1">✏️ 공지 작성</a>
			</div>
			<ul class="article-preview">
				<c:forEach var="article" items="${noticeArticles}">
					<li>
						<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}"> ${article.title} (
							${fn:substring(article.regDate, 0, 10)} ) </a>
					</li>
				</c:forEach>
				<c:if test="${empty noticeArticles}">
					<li>공지사항이 없습니다.</li>
				</c:if>
			</ul>
		</div>

		<!-- ✅ 자유게시판 섹션 -->
		<div class="content-box">
			<div class="section-title">
				📝 최근 자유게시판
				<a class="write-button" href="/usr/article/write?crewId=${crew.id}&boardId=3">✏️ 자유 글쓰기</a>
			</div>
			<ul class="article-preview">
				<c:forEach var="article" items="${freeArticles}">
					<li>
						<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}"> ${article.title} (
							${fn:substring(article.regDate, 0, 10)} ) </a>
					</li>
				</c:forEach>
				<c:if test="${empty freeArticles}">
					<li>자유 게시글이 없습니다.</li>
				</c:if>
			</ul>
		</div>

		<!-- ✅ 사진첩 섹션 -->
		<div class="content-box">
			<div class="section-title">
				📸 최근 사진
				<a class="write-button" href="/usr/article/write?crewId=${crew.id}&boardId=4">📤 사진 업로드</a>
			</div>
			<ul class="article-preview">
				<c:forEach var="article" items="${galleryArticles}">
					<li>
						<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}">
							<c:if test="${not empty article.imageUrl}">
								<img src="${article.imageUrl}" alt="사진" />
							</c:if>
							${article.title} (
							<fmt:formatDate value="${article.regDate}" pattern="yyyy-MM-dd" />
							)
						</a>
					</li>
				</c:forEach>
				<c:if test="${empty galleryArticles}">
					<li>사진이 없습니다.</li>
				</c:if>
			</ul>
		</div>

		<!-- ✅ 일정 등록 섹션 -->
		<div class="content-box calendar-box">
			<div class="section-title">
				📅 일정 등록
				<a class="write-button" href="/usr/article/writeSchedule?crewId=${crew.id}">➕ 일정 추가</a>
			</div>
			<p>달력을 클릭해서 일정을 등록하세요.</p>
			<div id="calendar" style="height: 300px; border: 1px solid #aaa; background: #fff;"></div>
		</div>
	</div>

</body>
</html>
