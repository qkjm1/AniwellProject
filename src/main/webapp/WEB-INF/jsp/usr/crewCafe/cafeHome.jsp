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

.section-title a.write-button, .section-title button.write-button {
	position: absolute;
	right: 0;
	font-size: 0.85em;
	text-decoration: none;
	color: #007bff;
	background: none;
	border: none;
	cursor: pointer;
}

.section-title a.write-button:hover, .section-title button.write-button:hover
	{
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
			<a href="javascript:void(0);" onclick="openGalleryModal()">📸 사진첩</a>
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
						<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}"> ${article.title}
							(${fn:substring(article.regDate, 0, 10)}) </a>
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
						<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}"> ${article.title}
							(${fn:substring(article.regDate, 0, 10)}) </a>
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
				<button onclick="openGalleryModal()" class="write-button" type="button">🖼 사진 더보기</button>
			</div>

			<ul class="article-preview" style="display: flex; flex-wrap: wrap; gap: 16px; list-style: none; padding: 0;">
				<c:forEach var="article" items="${galleryArticles}">
					<c:if test="${not empty article.imageUrl and article.imageUrl ne 'undefined'}">
						<li style="flex: 0 0 auto; width: 180px; text-align: center;">
							<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}" style="text-decoration: none; color: black;">
								<img src="${article.imageUrl}" alt="사진"
									style="width: 100%; max-height: 160px; object-fit: cover; border-radius: 8px; margin-bottom: 8px;" />
								<div style="font-weight: bold;">${article.title}</div>
								${fn:substring(article.regDate, 0, 10)}
							</a>
						</li>
					</c:if>
				</c:forEach>

				<c:if test="${empty galleryArticles}">
					<li>사진이 없습니다.</li>
				</c:if>
			</ul>
		</div>

		<!-- ✅ 사진 팝업 모달 -->
		<div id="galleryModal"
			style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.7); z-index: 9999; overflow-y: auto;">
			<div
				style="max-width: 960px; margin: 50px auto; padding: 20px; background: white; border-radius: 10px; position: relative;">
				<h2>📷 업로드된 사진</h2>
				<button onclick="closeGalleryModal()" style="position: absolute; top: 10px; right: 10px;">❌</button>

				<div style="display: flex; flex-wrap: wrap; gap: 16px;">
					<c:forEach var="article" items="${galleryArticles}">
						<c:if test="${not empty article.imageUrl and article.imageUrl ne 'undefined'}">
							<img src="${article.imageUrl}" alt="팝업 이미지" style="width: 200px; height: auto; border-radius: 8px;" />
						</c:if>
					</c:forEach>
				</div>
			</div>
		</div>


		<!-- ✅ 일정 등록 섹션 -->
		<div class="content-box calendar-box">
			<div class="section-title">
				📅 등록된 일정
				<button onclick="openScheduleModal()" class="write-button" type="button">➕ 일정 추가</button>
			</div>

			<!-- ✅ 일정 목록 출력 (팝업 열기 포함) -->
			<ul class="article-preview">
				<c:forEach var="schedule" items="${scheduleArticles}">
					<li>
						<a href="javascript:void(0);"
							onclick="openScheduleDetail('${schedule.scheduleDate}', '${fn:escapeXml(schedule.title)}', '${fn:escapeXml(schedule.body)}')">
							📅 ${schedule.scheduleDate} -
							<strong>${schedule.title}</strong>
						</a>
					</li>
				</c:forEach>
				<c:if test="${empty scheduleArticles}">
					<li>등록된 일정이 없습니다.</li>
				</c:if>
			</ul>
		</div>

		<!-- ✅ 일정 등록 모달 -->
		<div id="scheduleModal"
			style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; justify-content: center; align-items: center;">
			<div
				style="background: #fff; padding: 20px; border-radius: 10px; width: 400px; position: relative; margin: 100px auto;">
				<h3>📅 일정 등록</h3>
				<form action="/usr/article/doWriteSchedule" method="post">
					<input type="hidden" name="crewId" value="${crew.id}" />

					<div style="margin-bottom: 10px;">
						<label for="scheduleDate">날짜 선택:</label>
						<input type="date" id="scheduleDate" name="scheduleDate" required />
					</div>

					<div style="margin-bottom: 10px;">
						<label for="scheduleTitle">일정 내용:</label>
						<input type="text" id="scheduleTitle" name="scheduleTitle" required style="width: 100%;" />
					</div>
					<div style="margin-bottom: 10px;">
						<label for="scheduleBody">일정 설명:</label>
						<textarea id="scheduleBody" name="scheduleBody" rows="3" style="width: 100%;" placeholder="일정 상세 내용을 입력하세요"></textarea>
					</div>
					<div style="text-align: right;">
						<button type="submit">등록</button>
						<button type="button" onclick="closeScheduleModal()">취소</button>
					</div>
				</form>
			</div>
		</div>

		<!-- ✅ 일정 상세 보기 모달 -->
		<div id="scheduleDetailModal"
			style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); z-index: 9999; justify-content: center; align-items: center;">
			<div
				style="background: #fff; padding: 20px; border-radius: 10px; width: 400px; position: relative; margin: 100px auto;">
				<h3 id="detailScheduleTitle">📅 일정 제목</h3>
				<p>
					<strong>날짜:</strong>
					<span id="detailScheduleDate"></span>
				</p>
				<p>
					<strong>내용:</strong>
				</p>
				<p id="detailScheduleBody" style="white-space: pre-wrap;"></p>
				<div style="text-align: right;">
					<button type="button" onclick="closeScheduleDetailModal()">닫기</button>
				</div>
			</div>
		</div>

		<!-- ✅ JS 추가 -->
		<script>
			function openGalleryModal() {
				document.getElementById("galleryModal").style.display = "block";
			}
			function closeGalleryModal() {
				document.getElementById("galleryModal").style.display = "none";
			}
			function openScheduleModal() {
				document.getElementById("scheduleModal").style.display = "flex";
			}
			function closeScheduleModal() {
				document.getElementById("scheduleModal").style.display = "none";
			}

			// ✅ 일정 상세 팝업 열기
			function openScheduleDetail(date, title, body) {
				document.getElementById("detailScheduleDate").innerText = date;
				document.getElementById("detailScheduleTitle").innerText = "📅 "
						+ title;
				document.getElementById("detailScheduleBody").innerText = body;
				document.getElementById("scheduleDetailModal").style.display = "flex";
			}

			function closeScheduleDetailModal() {
				document.getElementById("scheduleDetailModal").style.display = "none";
			}
		</script>
</body>
</html>
