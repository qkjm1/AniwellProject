<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="게시글 작성" />
<%@ include file="/WEB-INF/jsp/usr/common/head.jspf"%>

<section class="mt-8 px-4 max-w-3xl mx-auto">
	<form method="POST" action="/usr/article/doWrite"
		class="space-y-6 bg-white p-6 rounded-xl shadow-md">

		<!-- ✅ 일반 게시판용 boardId 선택 -->
		<c:if test="${empty crewId}">
			<c:choose>
				<c:when test="${not empty boardId}">
					<!-- ✅ 이미 boardId가 넘어온 경우: hidden 처리 -->
					<input type="hidden" name="boardId" value="${boardId}" />
				</c:when>
				<c:otherwise>
					<!-- ✅ 직접 선택하는 경우: select 드롭다운 -->
					<div>
						<label for="boardId" class="block font-semibold mb-1">게시판
							선택</label> <select id="boardId" name="boardId" required
							class="select select-bordered w-full">
							<option value="">-- 게시판을 선택하세요 --</option>
							<c:if test="${rq.loginedMember.authLevel == 7}">
								<option value="1">공지사항</option>
							</c:if>
							<option value="2">크루모집</option>
						</select>
					</div>
				</c:otherwise>
			</c:choose>
		</c:if>



		<!-- ✅ 크루 게시판용 hidden 값 -->
		<c:if test="${not empty crewId}">
			<input type="hidden" name="crewId" value="${crewId}" />
			<input type="hidden" name="type" value="${type}" />
			<input type="hidden" name="boardId" value="${boardId}" />
			<!-- ✅ boardId도 반드시 함께 넘겨야 함 (예: 크루 게시판은 boardId 3) -->
			<!-- <input type="hidden" name="boardId" value="3" /> -->
		</c:if>


		<!-- 제목 입력 -->
		<div>
			<label for="title" class="block font-semibold mb-1">제목</label> <input
				type="text" id="title" name="title" required
				class="input input-bordered w-full" placeholder="제목을 입력하세요" />
		</div>

		<!-- 내용 입력 -->
		<div>
			<label for="body" class="block font-semibold mb-1">내용</label>
			<textarea id="body" name="body" rows="10" required
				class="textarea textarea-bordered w-full" placeholder="내용을 입력하세요"></textarea>
		</div>

		<!-- 이미지 업로드 -->
		<div>
			<label for="imageFile" class="block font-semibold mb-1">이미지
				업로드</label> <input type="file" name="imageFile" id="imageFile"
				accept="image/*" />
			<button type="button" onclick="uploadImage()"
				class="btn btn-sm btn-outline mt-2">이미지 업로드</button>
			<input type="hidden" name="imageUrl" id="imageUrl" />
			<div style="margin-top: 10px;">
				<img id="preview" style="max-width: 300px; display: none;" />
			</div>
		</div>
		<script>
function uploadImage() {
  const imageFile = document.getElementById("imageFile").files[0];

  if (!imageFile) {
    alert("이미지 파일을 선택해주세요.");
    return;
  }

  const formData = new FormData();
  formData.append("imageFile", imageFile);

  fetch("/usr/crewCafe/uploadImage", {
    method: "POST",
    body: formData
  })
    .then(response => response.json())
    .then(data => {
      if (data.resultCode === "S-1" && data.imageUrl) {
        const imageUrl = data.imageUrl;
        document.getElementById("imageUrl").value = imageUrl;
        document.getElementById("preview").src = imageUrl;
        document.getElementById("preview").style.display = "block";
        alert("이미지 업로드 성공!");
      } else {
        alert("이미지 업로드에 실패했습니다.");
      }
    })
    .catch(error => {
      console.error("업로드 오류:", error);
      alert("이미지 업로드 중 오류가 발생했습니다.");
    });
}

// ✅ 디버깅용: form submit 직전 imageUrl 확인
document.querySelector("form").addEventListener("submit", (e) => {
  console.log("최종 imageUrl 값:", document.getElementById("imageUrl").value);
});
</script>


		<!-- 버튼 -->
		<div class="flex gap-4">
			<button type="submit" class="btn btn-primary">등록</button>
			<button type="button" onclick="history.back();"
				class="btn btn-outline">뒤로가기</button>
		</div>

	</form>
</section>


<%@ include file="/WEB-INF/jsp/usr/common/foot.jspf"%>
