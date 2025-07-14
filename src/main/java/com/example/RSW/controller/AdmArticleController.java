package com.example.RSW.controller;

import com.example.RSW.service.ArticleService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/adm/article") // 관리자용 게시글 관련 URL을 처리하는 컨트롤러
public class AdmArticleController {

    @Autowired
    private ArticleService articleService; // 게시글 관련 서비스 의존성 주입

    // 게시글 리스트 페이지 요청 처리
    @RequestMapping("/list")
    public String showList(HttpServletRequest req, Model model,
                           @RequestParam(defaultValue = "1") int page, // 현재 페이지 번호 (기본값 1)
                           @RequestParam(defaultValue = "title") String searchKeywordTypeCode, // 검색 타입 (기본값: 제목)
                           @RequestParam(defaultValue = "") String searchKeyword) throws IOException { // 검색 키워드 (기본값: 없음)

        Rq rq = (Rq) req.getAttribute("rq"); // 로그인 정보 등 사용자 정보 객체 획득

        int itemsInAPage = 10; // 한 페이지에 보여줄 게시글 수
        int articlesCount = articleService.getArticleCount(0, searchKeywordTypeCode, searchKeyword);
        // boardId = 0은 전체 게시판을 의미하며, 조건에 맞는 게시글 수를 가져옴

        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
        // 전체 페이지 수 계산 (게시글 수 ÷ 페이지당 항목 수)

        List<Article> articles = articleService.getForPrintArticles(0, itemsInAPage, page, searchKeywordTypeCode, searchKeyword);
        // 해당 조건에 맞는 게시글 리스트 조회

        // 모델에 데이터 전달
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("articlesCount", articlesCount);
        model.addAttribute("searchKeywordTypeCode", searchKeywordTypeCode);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("articles", articles);
        model.addAttribute("page", page);

        return "adm/article/list"; // JSP 뷰 경로 반환
    }

    // 게시글 삭제 처리 (AJAX 호출 예상)
    @PostMapping("/doDelete")
    @ResponseBody
    public String doDelete(HttpServletRequest req, int id) {
        Rq rq = (Rq) req.getAttribute("rq"); // 로그인 정보 객체

        Article article = articleService.getArticleById(id); // 삭제 대상 게시글 조회

        if (article == null) {
            // 게시글이 존재하지 않는 경우 히스토리 백 처리
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 존재하지 않습니다.", id));
        }

        articleService.deleteArticle(id); // 게시글 삭제

        // 삭제 성공 후 리스트 페이지로 리다이렉트
        return Ut.jsReplace("S-1", Ut.f("%d번 게시글을 삭제했습니다.", id), "/adm/article/list");
    }
}
