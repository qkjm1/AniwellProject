package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.vo.Qna;
import com.example.RSW.vo.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/adm/qna") // 관리자용 QnA 관리 컨트롤러
public class AdmQnaController {

    @Autowired
    private Rq rq; // 로그인 정보 객체

    @Autowired
    private QnaService qnaService; // QnA 관련 서비스

    @Autowired
    private VetAnswerService vetAnswerService; // 수의사 답변 관련 서비스

    // QnA 목록 페이지
    @GetMapping("/list")
    public String showQnaList(Model model) {
        model.addAttribute("qnaList", qnaService.findAll()); // 모든 QnA 조회
        return "adm/qna/list"; // 리스트 JSP 반환
    }

    // QnA 상세 페이지
    @GetMapping("/detail")
    public String showQnaDetail(@RequestParam int id, Model model) {
        model.addAttribute("qna", qnaService.findById(id)); // QnA 본문
        model.addAttribute("answer", vetAnswerService.findByQnaId(id)); // 수의사 답변
        return "adm/qna/detail"; // 상세 페이지 JSP
    }

    // 수의사 답변 등록
    @PostMapping("/doAnswer")
    public String doAnswer(@RequestParam int qnaId, @RequestParam String answer) {
        String vetName = rq.getLoginedMember().getNickname(); // 로그인한 수의사의 닉네임
        vetAnswerService.write(qnaId, answer, vetName); // 답변 저장
        return "redirect:/adm/qna/detail?id=" + qnaId; // 상세 페이지로 리다이렉트
    }

    // 수의사 답변 수정
    @PostMapping("/doUpdateAnswer")
    public String doUpdateAnswer(@RequestParam int id, @RequestParam String answer) {
        vetAnswerService.update(id, answer); // 답변 수정
        return "redirect:/adm/qna/detail?id=" + vetAnswerService.getQnaIdByAnswerId(id); // 해당 QnA 상세로 이동
    }

    // 수의사 답변 삭제
    @PostMapping("/doDeleteAnswer")
    public String doDeleteAnswer(@RequestParam int id) {
        long qnaId = vetAnswerService.getQnaIdByAnswerId(id); // 해당 QnA ID 조회
        vetAnswerService.delete(id); // 답변 삭제
        return "redirect:/adm/qna/detail?id=" + qnaId; // QnA 상세로 리다이렉트
    }

    // QnA 수정 폼
    @GetMapping("/edit")
    public String showEditForm(@RequestParam int id, Model model) {
        model.addAttribute("qna", qnaService.findById(id)); // 수정할 QnA 로드
        return "adm/qna/edit"; // 수정 JSP
    }

    // QnA 본문 수정 처리
    @PostMapping("/doModify")
    public String doModify(@RequestParam int id,
                           @RequestParam String title,
                           @RequestParam String body) {
        qnaService.modify(id, title, body); // 제목/본문 수정
        return "redirect:/adm/qna/detail?id=" + id; // 상세로 리다이렉트
    }

    // QnA 삭제 처리
    @PostMapping("/doDelete")
    public String doDelete(@RequestParam int id) {
        System.out.println("삭제 요청 id: " + id); // 디버깅용 로그
        qnaService.delete(id); // 삭제
        return "redirect:/adm/qna/list"; // 목록으로 이동
    }

    // FAQ 등록 폼
    @GetMapping("/write")
    public String showWriteForm() {
        return "adm/qna/write"; // FAQ 작성 폼
    }

    // FAQ 등록 처리
    @PostMapping("/doWrite")
    public String doWrite(@RequestParam String title, @RequestParam String body) {
        int memberId = rq.getLoginedMemberId(); // 로그인한 관리자 ID
        qnaService.writeFaq(memberId, title, body); // FAQ로 저장 (isFaq = 1)
        return "redirect:/adm/qna/list"; // 목록으로 이동
    }
}
