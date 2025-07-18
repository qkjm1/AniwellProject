<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:choose>
	<c:when test="${not empty crew}">
		<c:set var="pageTitle" value="${crew.title} 게시판 목록" />
	</c:when>
	<c:otherwise>
		<c:set var="pageTitle" value="${board.code} LIST" />
	</c:otherwise>
</c:choose>

<%@ include file="../common/head.jspf"%>

<section class="mt-24 text-xl px-4">
	<div class="mx-auto">
		<div class="mb-4 flex">
			<div>${articlesCount}개</div>
			<div class="flex-grow"></div>
			<form action="">
				<c:if test="${not empty crew}">
					<input type="hidden" name="crewId" value="${crew.id}" />
				</c:if>
				<c:if test="${not empty board}">
					<input type="hidden" name="boardId" value="${param.boardId}" />
				</c:if>
				<div class="flex">
					<select class="select select-sm select-bordered max-w-xs" name="searchKeywordTypeCode">
						<option value="title" ${param.searchKeywordTypeCode == 'title' ? 'selected' : ''}>title</option>
						<option value="body" ${param.searchKeywordTypeCode == 'body' ? 'selected' : ''}>body</option>
						<option value="title,body" ${param.searchKeywordTypeCode == 'title,body' ? 'selected' : ''}>title+body</option>
						<option value="nickname" ${param.searchKeywordTypeCode == 'nickname' ? 'selected' : ''}>nickname</option>
					</select>

					<label class="ml-3 input input-bordered input-sm flex items-center gap-2">
						<input type="text" placeholder="Search" name="searchKeyword" value="${param.searchKeyword}" />
						<button type="submit">
							<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" class="h-4 w-4 opacity-70">
                                <path fill-rule="evenodd"
									d="M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z"
									clip-rule="evenodd" />
                            </svg>
						</button>
					</label>
				</div>
			</form>
		</div>

		<table class="table" border="1" cellspacing="0" cellpadding="5" style="width: 100%; border-collapse: collapse;">
			<thead>
				<tr>
					<th style="text-align: center;">ID</th>
					<th style="text-align: center;">등록일</th>
					<th style="text-align: center;">제목</th>
					<th style="text-align: center;">작성자</th>
					<th style="text-align: center;">조회수</th>
					<th style="text-align: center;">좋아요</th>
					<th style="text-align: center;">싫어요</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="article" items="${articles}">
					<tr class="hover:bg-base-300">
						<td style="text-align: center;">${article.id}</td>
						<td style="text-align: center;">${article.regDate.substring(0, 10)}</td>
						<td style="text-align: center;">
							<a class="hover:underline"
								href="detail?id=${article.id}<c:if test='${not empty crew}'> &crewId=${crew.id}</c:if><c:if test='${not empty board}'> &boardId=${board.id}</c:if>">
								${article.title}
								<c:if test="${article.extra__repliesCount > 0}">
									<span style="color: red;">[${article.extra__repliesCount}]</span>
								</c:if>
							</a>
						</td>
						<td style="text-align: center;">${article.extra__writer}</td>
						<td style="text-align: center;">${article.hitCount}</td>
						<td style="text-align: center;">${article.goodReactionPoint}</td>
						<td style="text-align: center;">${article.badReactionPoint}</td>
					</tr>
				</c:forEach>

				<c:if test="${empty articles}">
					<tr>
						<td colspan="7" style="text-align: center;">게시글이 없습니다</td>
					</tr>
				</c:if>
			</tbody>
		</table>
	</div>

	<!-- ✅ 동적 페이징 -->
	<div class="flex justify-center mt-4">
		<div class="btn-group join">
			<c:set var="paginationLen" value="3" />
			<c:set var="startPage" value="${page - paginationLen >= 1 ? page - paginationLen : 1}" />
			<c:set var="endPage" value="${page + paginationLen <= pagesCount ? page + paginationLen : pagesCount}" />

			<c:set var="baseUri" value="?" />
			<c:if test="${not empty crew}">
				<c:set var="baseUri" value="${baseUri}crewId=${crew.id}" />
			</c:if>
			<c:if test="${not empty board}">
				<c:set var="baseUri" value="${baseUri}boardId=${board.id}" />
			</c:if>
			<c:set var="baseUri" value="${baseUri}&searchKeywordTypeCode=${searchKeywordTypeCode}&searchKeyword=${searchKeyword}" />

			<c:if test="${startPage > 1}">
				<a class="join-item btn btn-sm" href="${baseUri}&page=1">1</a>
			</c:if>

			<c:if test="${startPage > 2}">
				<button class="join-item btn btn-sm btn-disabled">...</button>
			</c:if>

			<c:forEach begin="${startPage}" end="${endPage}" var="i">
				<a class="join-item btn btn-sm ${param.page == i ? 'btn-active' : ''}" href="${baseUri}&page=${i}">${i}</a>
			</c:forEach>

			<c:if test="${endPage < pagesCount - 1}">
				<button class="join-item btn-sm btn btn-disabled">...</button>
			</c:if>

			<c:if test="${endPage < pagesCount}">
				<a class="join-item btn btn-sm" href="${baseUri}&page=${pagesCount}">${pagesCount}</a>
			</c:if>
		</div>
	</div>
</section>

<%@ include file="../common/foot.jspf"%>
