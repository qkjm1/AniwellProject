package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class UsrQnaController {

    @Autowired
    private Rq rq;

    @Autowired
    private QnaService qnaService;

    @Autowired
    private VetAnswerService vetAnswerService;

    // 질문 리스트 페이지 (자주 묻는 질문 + 내가 한 질문)
    @RequestMapping("/usr/qna/list")
    public String showQnaList(Model model,
                              @RequestParam(value = "selectedId", required = false) Integer selectedId) {
        List<Qna> qnas = qnaService.getPublicFaqList(); // 공개 FAQ 목록
        List<Qna> myQnas = qnaService.getUserQnaByMemberId(rq.getLoginedMemberId()); // 내가 한 질문 목록

        Qna selectedQna = null;
        if (selectedId != null) {
            selectedQna = qnaService.getQnaById(selectedId); // 선택된 질문
        }

        model.addAttribute("qnas", qnas);
        model.addAttribute("myQnas", myQnas);
        model.addAttribute("selectedQna", selectedQna);
        return "usr/qna/list"; // JSP 렌더링
    }

    // 질문 상세 보기 (수의사 답변 포함)
    @RequestMapping("/usr/qna/detail")
    public String showQnaDetail(Model model, int id) {
        Qna qna = qnaService.getQnaById(id);
        model.addAttribute("qna", qna);

        List<VetAnswer> vetAnswers = vetAnswerService.getByQnaId(id); // 수의사 답변 리스트
        model.addAttribute("vetAnswers", vetAnswers);

        model.addAttribute("rq", rq);
        return "usr/qna/detail"; // JSP 경로
    }

    // 질문 등록 폼
    @RequestMapping("/usr/qna/ask")
    public String showQnaAskForm() {
        return "usr/qna/ask";
    }

    @RequestMapping(value = "/usr/qna/doAsk", method = RequestMethod.POST)
    @ResponseBody
    public ResultData doAsk(@RequestParam String title,
                            @RequestParam String body,
                            @RequestParam(required = false, defaultValue = "false") boolean isSecret) {

        int loginedMemberId = rq.getLoginedMemberId();

        // ✅ 로그인 여부 확인 (추가)
        if (loginedMemberId == 0) {
            return ResultData.from("F-L", "로그인 후 이용해주세요.");
        }

        if (Ut.isEmptyOrNull(title)) return ResultData.from("F-1", "제목을 입력해주세요.");
        if (Ut.isEmptyOrNull(body)) return ResultData.from("F-2", "내용을 입력해주세요.");

        qnaService.writeUserQna(loginedMemberId, title, body, isSecret);

        return ResultData.from("S-1", "질문이 성공적으로 등록되었습니다.");
    }


    // 질문 삭제 처리
    @RequestMapping("/usr/qna/doDelete")
    @ResponseBody
    public void doDelete(@RequestParam int id) throws IOException {
        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            rq.printHistoryBack("존재하지 않는 질문입니다.");
            return;
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            rq.printHistoryBack("권한이 없습니다.");
            return;
        }

        qnaService.deleteQna(id);
        rq.printReplace("S-1", "질문이 삭제되었습니다.", "/usr/qna/list");
    }

    // 질문 수정 폼
    @RequestMapping("/usr/qna/modify")
    public String showModifyForm(@RequestParam int id, Model model) {
        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            return rq.historyBackOnView("존재하지 않는 질문입니다.");
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            return rq.historyBackOnView("권한이 없습니다.");
        }

        model.addAttribute("qna", qna);
        return "usr/qna/modify"; // 수정 폼 JSP
    }

    // 질문 수정 처리
    @RequestMapping(value = "/usr/qna/doModify", method = RequestMethod.POST)
    @ResponseBody
    public String doModify(@RequestParam int id,
                           @RequestParam String title,
                           @RequestParam String body,
                           @RequestParam(defaultValue = "false") boolean isSecret) throws IOException {

        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            return rq.historyBackOnView("존재하지 않는 질문입니다.");
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            return rq.historyBackOnView("권한이 없습니다.");
        }

        qnaService.modifyQna(id, title, body, isSecret);

        rq.printReplace("S-1", "질문이 수정되었습니다.", "/usr/qna/detail?id=" + id);
        return null; // 리턴하지 않음 (자바스크립트로 리다이렉트 처리)
    }
}
