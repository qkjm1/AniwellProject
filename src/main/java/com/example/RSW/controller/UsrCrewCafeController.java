package com.example.RSW.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Article;
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.service.ArticleService;
import com.example.RSW.service.DistrictService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.Date;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequestMapping("/usr/crewCafe")
@Controller
public class UsrCrewCafeController {

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private ArticleService articleService;

	@GetMapping("")
	public String showCafeMain(@RequestParam(required = false) Integer crewId, Model model) {
		if (crewId == null) {
			// crewId가 비어있을 경우 처리
			return "redirect:/usr/crewCafe/list"; // 또는 안내 페이지
		}

		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null) {
			return "common/notFound";
		}

		model.addAttribute("crew", crew);
		return "usr/crewCafe/cafeHome";
	}

	// ✅ 특정 크루의 게시글 목록 페이지
	@GetMapping("/article/list")
	public String showCrewArticleList(@RequestParam int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		List<Article> articles = articleService.getArticlesByCrewId(crewId);
		WalkCrew crew = walkCrewService.getCrewById(crewId);

		model.addAttribute("crew", crew);
		model.addAttribute("articles", articles);

		return "usr/crewCafe/articleList";
	}

	// ✅ 게시글 작성 폼 페이지
	@GetMapping("/article/write")
	public String showCrewArticleWrite(@RequestParam int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return "redirect:/usr/member/login?msg=로그인 후 이용해주세요.";
		}

		boolean isApproved = walkCrewService.isApprovedMember(crewId, rq.getLoginedMemberId());
		if (!isApproved) {
			return "common/permissionDenied";
		}

		model.addAttribute("crewId", crewId);
		return "usr/crewCafe/articleWrite";
	}

	// ✅ 게시글 등록 처리
	@PostMapping("/article/doWrite")
	@ResponseBody
	public String doCrewArticleWrite(HttpServletRequest req, @RequestParam int crewId, @RequestParam String title,
			@RequestParam String body) {

		Rq rq = (Rq) req.getAttribute("rq");

		ResultData rd = articleService.writeCrewArticle(crewId, rq.getLoginedMemberId(), title, body);
		int newArticleId = (int) rd.getData1();

		return Ut.jsReplace(rd.getResultCode(), rd.getMsg(),
				"/usr/crewCafe/article/detail?id=" + newArticleId + "&crewId=" + crewId);
	}
}
