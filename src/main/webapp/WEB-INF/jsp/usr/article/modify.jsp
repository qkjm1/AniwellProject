<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="게시글 수정" />
<%@ include file="/WEB-INF/jsp/usr/common/head.jspf"%>

<section class="mt-8 px-4 max-w-3xl mx-auto">
	<form method="POST" action="/usr/article/doModify" class="space-y-6 bg-white p-6 rounded-xl shadow-md">

		<!-- 게시글 ID -->
		<input type="hidden" name="id" value="${article.id}" />

		<!-- boardId 유지 -->
		<c:if test="${empty crewId}">
			<input type="hidden" name="boardId" value="${article.boardId}" />
		</c:if>
		<c:if test="${not empty crewId}">
			<input type="hidden" name="crewId" value="${crewId}" />
			<input type="hidden" name="type" value="${type}" />
			<input type="hidden" name="boardId" value="${article.boardId}" />
		</c:if>

		<!-- 제목 수정 -->
		<div>
			<label for="title" class="block font-semibold mb-1">제목</label>
			<input type="text" id="title" name="title" required class="input input-bordered w-full" value="${article.title}" />
		</div>

		<!-- 내용 수정 -->
		<div>
			<label for="body" class="block font-semibold mb-1">내용</label>
			<textarea id="body" name="body" rows="10" required class="textarea textarea-bordered w-full">${article.body}</textarea>
		</div>

		<!-- 이미지 업로드는 현재 doModify()에서 처리 안 하므로 생략 또는 향후 확장 -->
		<!-- <div>... 생략 가능 ...</div> -->

		<!-- 버튼 -->
		<div class="flex gap-4">
			<button type="submit" class="btn btn-primary">수정 완료</button>
			<button type="button" onclick="history.back();" class="btn btn-outline">취소</button>
		</div>
	</form>
</section>

<%@ include file="/WEB-INF/jsp/usr/common/foot.jspf"%>
