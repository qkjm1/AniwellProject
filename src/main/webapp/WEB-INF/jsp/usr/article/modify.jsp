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


		<!-- ✅ 기존 이미지 미리보기 -->
		<c:if test="${not empty article.imageUrl}">
			<div>
				<label class="block font-semibold mb-1">기존 이미지</label>
				<img id="preview" src="${article.imageUrl}" alt="기존 업로드 이미지" style="max-width: 300px; border-radius: 8px;" />
			</div>
		</c:if>
		<!-- ✅ 이미지 업로드 -->
		<div>
			<label for="imageFile" class="block font-semibold mb-1">새 이미지 업로드</label>
			<input type="file" name="imageFile" id="imageFile" accept="image/*" onchange="previewImage(this)" />
			<!-- 업로드 후 저장될 이미지 URL -->
			<input type="hidden" name="imageUrl" id="imageUrl" value="${article.imageUrl}" />
		</div>
		<!-- 버튼 -->
		<div class="flex gap-4">
			<button type="submit" class="btn btn-primary">수정 완료</button>
			<button type="button" onclick="history.back();" class="btn btn-outline">취소</button>
		</div>
	</form>
</section>

<%@ include file="/WEB-INF/jsp/usr/common/foot.jspf"%>
