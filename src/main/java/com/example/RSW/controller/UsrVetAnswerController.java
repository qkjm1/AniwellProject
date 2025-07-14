package com.example.RSW.controller;

import com.example.RSW.service.NotificationService;
import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Qna;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.VetAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usr/vetAnswer") // 사용자(수의사)용 답변 기능 컨트롤러
public class UsrVetAnswerController {

    @Autowired
    private VetAnswerService vetAnswerService;

    @Autowired
    private QnaService qnaService;

    @Autowired
    private Rq rq;

    @Autowired
    private NotificationService notificationService;

    // 답변 등록 처리
    @PostMapping("/doWrite")
    @ResponseBody
    public String doWrite(@RequestParam("qnaId") int qnaId,
                          @RequestParam("answer") String answer) {

        Member loginedMember = rq.getLoginedMember();

        // 1. 로그인 및 수의사 권한 확인
        if (loginedMember == null || loginedMember.getAuthLevel() != 3) {
            return Ut.jsHistoryBack("F-1", "수의사만 답변할 수 있습니다.");
        }

        // 2. 질문 존재 여부 확인
        Qna qna = qnaService.getQnaById(qnaId);
        if (qna == null) {
            return Ut.jsHistoryBack("F-2", "해당 질문이 존재하지 않습니다.");
        }

        // 3. 이미 답변이 달린 질문인지 확인
        if (qna.isAnswered()) {
            return Ut.jsHistoryBack("F-3", "이미 답변이 등록된 질문입니다.");
        }

        // 4. 답변 저장
        vetAnswerService.write(qnaId, loginedMember.getId(), answer, loginedMember.getNickname());

        // 5. 질문 상태를 '답변 완료'로 변경
        qnaService.markAsAnswered(qnaId);

        // ✅ 알림 전송 (질문자 본인에게만, 수의사 자신이 아닐 경우)
        if (qna.getMemberId() != loginedMember.getId()) {
            String title = "수의사로부터 답변이 등록되었습니다.";
            String link = "/usr/qna/detail?id=" + qnaId;
            notificationService.send(qna.getMemberId(), title, link);
        }

        // 6. 성공 메시지 후 질문 상세로 이동
        return Ut.jsReplace("S-1", "답변이 등록되었습니다.", "/usr/qna/detail?id=" + qnaId);
    }

    // 답변 수정 폼
    @GetMapping("/modify")
    public String showModifyForm(@RequestParam int id, Model model) {
        VetAnswer vetAnswer = vetAnswerService.getById(id);
        Member loginedMember = rq.getLoginedMember();

        if (vetAnswer == null) {
            return rq.historyBackOnView("존재하지 않는 답변입니다.");
        }

        // 본인만 수정 가능
        if (loginedMember == null || loginedMember.getId() != vetAnswer.getMemberId()) {
            return rq.historyBackOnView("권한이 없습니다.");
        }

        model.addAttribute("vetAnswer", vetAnswer);
        return "usr/vetAnswer/modify";
    }

    // 답변 수정 처리
    @PostMapping("/doModify")
    @ResponseBody
    public String doModify(@RequestParam int id,
                           @RequestParam String answer) {

        VetAnswer vetAnswer = vetAnswerService.getById(id);
        Member loginedMember = rq.getLoginedMember();

        if (vetAnswer == null) {
            return Ut.jsHistoryBack("F-1", "존재하지 않는 답변입니다.");
        }

        if (loginedMember == null || loginedMember.getId() != vetAnswer.getMemberId()) {
            return Ut.jsHistoryBack("F-2", "권한이 없습니다.");
        }

        vetAnswerService.modify(id, answer);

        return Ut.jsReplace("S-1", "답변이 수정되었습니다.", "/usr/qna/detail?id=" + vetAnswer.getQnaId());
    }

    // 답변 삭제 처리
    @PostMapping("/doDelete")
    @ResponseBody
    public String doDelete(@RequestParam int id) {
        VetAnswer vetAnswer = vetAnswerService.getById(id);
        Member loginedMember = rq.getLoginedMember();

        if (vetAnswer == null) {
            return Ut.jsHistoryBack("F-1", "존재하지 않는 답변입니다.");
        }

        if (loginedMember == null || loginedMember.getId() != vetAnswer.getMemberId()) {
            return Ut.jsHistoryBack("F-2", "권한이 없습니다.");
        }

        vetAnswerService.delete(id);

        // 필요 시 질문을 '답변 미완료' 상태로 변경
        qnaService.markAsAnsweredFalse(vetAnswer.getQnaId());

        return Ut.jsReplace("S-1", "답변이 삭제되었습니다.", "/usr/qna/detail?id=" + vetAnswer.getQnaId());
    }

    // 수의사 전용 질문 목록 페이지
    @RequestMapping("/vetList")
    public String showVetQnaList(Model model) {
        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null || loginedMember.getAuthLevel() != 3) {
            return "redirect:/usr/member/login"; // 비로그인 또는 수의사 아님
        }

        List<Qna> questions = qnaService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("rq", rq);

        return "usr/vetAnswer/vetList"; // 수의사 질문 목록 JSP
    }
}
