package com.example.RSW.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.RSW.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.interceptor.BeforeActionInterceptor;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.Board;
import com.example.RSW.vo.Reply;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UsrArticleController {

	private final BeforeActionInterceptor beforeActionInterceptor;

	@Autowired
	private Rq rq;

	@Autowired
	private ArticleService articleService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private ReactionPointService reactionPointService;

	@Autowired
	private ReplyService replyService;

	@Autowired
	private Cloudinary cloudinary;

	UsrArticleController(BeforeActionInterceptor beforeActionInterceptor) {
		this.beforeActionInterceptor = beforeActionInterceptor;
	}

	@RequestMapping("/usr/article/write")
	public String showWrite(HttpServletRequest req, @RequestParam(required = false) Integer boardId,
			@RequestParam(required = false) Integer crewId, @RequestParam(required = false) String type, Model model) {

		Rq rq = (Rq) req.getAttribute("rq");

		System.out.println("🔥 /usr/article/write 진입");
		System.out.println("📌 crewId = " + crewId);
		System.out.println("📌 loginedMemberId = " + rq.getLoginedMemberId());


		// ✅ 크루 글쓰기 처리일 경우
		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);

			// ❌ 존재하지 않는 크루인 경우
			if (crew == null) {
				req.setAttribute("msg", "F-1 / 존재하지 않는 크루입니다.");
				req.setAttribute("historyBack", true);
				return "common/js"; // JS를 이용한 경고 후 이전 페이지로
			}

			// ❌ 승인되지 않은 멤버인 경우
			boolean isApproved = walkCrewService.isApprovedMember(crewId, rq.getLoginedMemberId());
			if (!isApproved) {
				req.setAttribute("msg", "F-2 / 승인된 크루 멤버만 글쓰기 가능합니다.");
				req.setAttribute("historyBack", true);
				return "common/js";
			}

			// ❌ 공지사항 게시판인데 크루장이 아닌 경우
			if (boardId != null && boardId == 1) {
				boolean isLeader = walkCrewService.isCrewLeader(crewId, rq.getLoginedMemberId());
				if (!isLeader) {
					req.setAttribute("msg", "F-3 / 공지사항은 크루장만 작성할 수 있습니다.");
					req.setAttribute("historyBack", true);
					return "usr/common/js";

				}
			}

			// ✅ 크루 정보와 게시판 정보 JSP로 전달
			model.addAttribute("crew", crew);
			model.addAttribute("crewId", crewId);
			model.addAttribute("type", type);

			model.addAttribute("boardId", boardId);

			System.out.println("✅ 글쓰기 진입 성공 (크루)");
			return "usr/article/write"; // 글쓰기 JSP 페이지로 이동
		}

		// ✅ 일반 게시판일 경우 boardId가 없으면 기본값으로 설정
		if (boardId == null) {
			boardId = 2;
			System.out.println("📌 기본 boardId 할당됨 = " + boardId);
		}

		System.out.println("✅ 글쓰기 진입 성공 (일반)");
		return "usr/article/write"; // 일반 글쓰기 JSP로 이동

	}

	@PostMapping("/usr/article/doWrite")
	@ResponseBody
	public String doWrite(HttpServletRequest req, @RequestParam(required = false) Integer crewId,
			@RequestParam(required = false) Integer boardId, @RequestParam String title, @RequestParam String body,
			@RequestParam(required = false) MultipartFile imageFile) {

		Rq rq = (Rq) req.getAttribute("rq");
		int loginedMemberId = rq.getLoginedMemberId();

		String imageUrl = null;

		// ✅ 이미지 파일이 있다면 Cloudinary 업로드 시도
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
				imageUrl = (String) uploadResult.get("secure_url");
				System.out.println("✅ 업로드 성공: " + imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// ✅ 크루 글과 일반 글 구분 처리
		ResultData rd;
		if (crewId != null) {

			rd = articleService.writeCrewArticle(boardId, crewId, loginedMemberId, title, body, imageUrl);

			return Ut.jsReplace(rd.getResultCode(), rd.getMsg(),
					"../article/detail?id=" + rd.getData1() + "&crewId=" + crewId);
		} else {
			rd = articleService.writeArticle(loginedMemberId, title, body, String.valueOf(boardId), imageUrl);
			return Ut.jsReplace(rd.getResultCode(), rd.getMsg(),
					"../article/detail?id=" + rd.getData1() + "&boardId=" + boardId);
		}
	}

	@RequestMapping("/usr/article/doModify")
	@ResponseBody
	public String doModify(HttpServletRequest req, int id, String title, String body) {
		Rq rq = (Rq) req.getAttribute("rq");
		Article article = articleService.getArticleById(id);

		if (article == null) {
			return Ut.jsReplace("F-1", Ut.f("%d번 게시글은 없습니다", id), "../article/list");
		}

		ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);
		if (userCanModifyRd.isFail()) {
			return Ut.jsHistoryBack(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
		}

		articleService.modifyArticle(id, title, body);
		return Ut.jsReplace(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg(), "../article/detail?id=" + id);
	}

	@RequestMapping("/usr/article/modify")
	public String showModify(HttpServletRequest req, Model model, @RequestParam int id) {
		Rq rq = (Rq) req.getAttribute("rq");
		Article article = articleService.getArticleById(id);

		if (article == null) {
			return Ut.jsHistoryBack("F-1", "존재하지 않는 게시물입니다.");
		}

		ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);
		if (userCanModifyRd.isFail()) {
			return Ut.jsHistoryBack(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
		}

		model.addAttribute("article", article);
		return "usr/article/modify";
	}

	@RequestMapping("/usr/article/doDelete")
	@ResponseBody
	public String doDelete(HttpServletRequest req, int id, @RequestParam int crewId) {
		Rq rq = (Rq) req.getAttribute("rq");
		Article article = articleService.getArticleById(id);

		if (article == null) {
			return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
		}

		ResultData userCanDeleteRd = articleService.userCanDelete(rq.getLoginedMemberId(), article);
		if (userCanDeleteRd.isFail()) {
			return Ut.jsHistoryBack(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg());
		}

		articleService.deleteArticle(id);

		return Ut.jsReplace("S-1", "게시글이 삭제되었습니다.", "../crewCafe/cafeHome?crewId=" + crewId);
	}

	@RequestMapping("/usr/article/detail")
	public String showDetail(HttpServletRequest req, Model model, int id,
			@RequestParam(required = false) Integer crewId, @RequestParam(required = false) Integer boardId) {
		Rq rq = (Rq) req.getAttribute("rq");
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

		ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), "article", id);
		if (usersReactionRd.isSuccess()) {
			model.addAttribute("userCanMakeReaction", true);
		}

		List<Reply> replies = replyService.getForPrintReplies(rq.getLoginedMemberId(), "article", id);
		model.addAttribute("replies", replies);
		model.addAttribute("repliesCount", replies.size());

		model.addAttribute("article", article);
		model.addAttribute("usersReaction", usersReactionRd.getData1());
		model.addAttribute("isAlreadyAddGoodRp",
				reactionPointService.isAlreadyAddGoodRp(rq.getLoginedMemberId(), id, "article"));
		model.addAttribute("isAlreadyAddBadRp",
				reactionPointService.isAlreadyAddBadRp(rq.getLoginedMemberId(), id, "article"));

		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			model.addAttribute("crew", crew);
		} else if (boardId != null) {
			Board board = boardService.getBoardById(boardId);
			model.addAttribute("board", board);
		}

		return "usr/article/detail";
	}

	@RequestMapping("/usr/article/list")
	public String showList(HttpServletRequest req, Model model, @RequestParam(required = false) Integer boardId,
			@RequestParam(required = false) Integer crewId, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "title") String searchKeywordTypeCode,
			@RequestParam(defaultValue = "") String searchKeyword) throws IOException {

		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ crewId와 boardId가 모두 있을 경우 (크루 게시판 구분된 글)
		if (crewId != null && boardId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			Board board = boardService.getBoardById(boardId);
			if (crew == null || board == null) {
				return rq.historyBackOnView("존재하지 않는 크루 또는 게시판");
			}

			List<Article> articles = articleService.getArticlesByCrewIdAndBoardId(crewId, boardId);
			model.addAttribute("crew", crew);
			model.addAttribute("board", board);
			model.addAttribute("articles", articles);
			model.addAttribute("page", page);
			return "usr/article/list";
		}

		// ✅ crewId만 있는 경우 (크루 전체 글 보기)
		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			List<Article> articles = articleService.getArticlesByCrewId(crewId);
			model.addAttribute("crew", crew);
			model.addAttribute("articles", articles);
			return "usr/article/list";
		}

		// ✅ 일반 게시판 (공지사항 등)
		if (boardId != null) {
			Board board = boardService.getBoardById(boardId);
			if (board == null) {
				return rq.historyBackOnView("존재하지 않는 게시판");
			}

			int itemsInAPage = 10;
			int articlesCount;
			int pagesCount;
			List<Article> articles;

			// ✅ boardId == 1 (전체 공지사항)인 경우, 관리자만 출력
			if (boardId == 1) {
				articlesCount = articleService.getAdminOnlyArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
				pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
				articles = articleService.getAdminOnlyArticles(boardId, itemsInAPage * (page - 1), itemsInAPage,
						searchKeywordTypeCode, searchKeyword);
			} else {
				articlesCount = articleService.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
				pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
				articles = articleService.getForPrintArticles(boardId, itemsInAPage * (page - 1), itemsInAPage,
						searchKeywordTypeCode, searchKeyword);
			}

			model.addAttribute("pagesCount", pagesCount);
			model.addAttribute("articlesCount", articlesCount);
			model.addAttribute("searchKeywordTypeCode", searchKeywordTypeCode);
			model.addAttribute("searchKeyword", searchKeyword);
			model.addAttribute("articles", articles);
			model.addAttribute("boardId", boardId);
			model.addAttribute("board", board);
			model.addAttribute("page", page);

			return "usr/article/list";
		}

		return "common/error";
	}

	@RequestMapping("/usr/article/doIncreaseHitCountRd")
	@ResponseBody
	public ResultData doIncreaseHitCount(int id) {
		ResultData increaseHitCountRd = articleService.increaseHitCount(id);
		if (increaseHitCountRd.isFail()) {
			return increaseHitCountRd;
		}

		return ResultData.newData(increaseHitCountRd, "hitCount", articleService.getArticleHitCount(id));
	}

	// 모임일정등록
	@PostMapping("/usr/article/doWriteSchedule")
	public String doWriteSchedule(@RequestParam int crewId, @RequestParam String scheduleDate,
			@RequestParam String scheduleTitle, @RequestParam(required = false) String scheduleBody,
			HttpServletRequest req) {

		Rq rq = (Rq) req.getAttribute("rq");
		int loginedMemberId = rq.getLoginedMemberId();

		// 저장 로직 (예시)
		articleService.writeSchedule(crewId, loginedMemberId, scheduleDate, scheduleTitle, scheduleBody);

		return "redirect:/usr/crewCafe/cafeHome?crewId=" + crewId;
	}

	// 모임일정 리스트
	@RequestMapping("/usr/article/schedule")
	public String showSchedule(@RequestParam int crewId, Model model) {
		List<Map<String, Object>> scheduleList = articleService.getSchedulesByCrewId(crewId);
		model.addAttribute("scheduleList", scheduleList);
		return "usr/article/schedule";
	}

}