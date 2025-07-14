package com.example.RSW.controller;

import com.example.RSW.service.MemberService;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.VetCertificateService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/adm/member")
public class AdmMemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private VetCertificateService vetCertificateService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private Rq rq;

    @RequestMapping("/list")
    public String showMemberList(@RequestParam(defaultValue = "") String searchType,
                                 @RequestParam(defaultValue = "") String searchKeyword,
                                 Model model) {
        List<Member> members = memberService.getForPrintMembers(searchType, searchKeyword);
        model.addAttribute("members", members);
        return "adm/member/list";
    }


    @PostMapping("/changeVetCertStatus")
    public String changeVetCertStatus(@RequestParam int memberId, @RequestParam int approved) {
        // 인증 상태 변경
        vetCertificateService.updateApprovalStatusByMemberId(memberId, approved);

        // 권한 자동 설정
        if (approved == 1) {
            memberService.updateAuthLevel(memberId, 3); // 수의사
        } else if (approved == 2) {
            memberService.updateAuthLevel(memberId, 1); // 일반
        }

        // 알림 전송
        String title = (approved == 1) ? "수의사 인증이 승인되었습니다." : "수의사 인증이 거절되었습니다.";
        String type = (approved == 1) ? "VET_APPROVED" : "VET_REJECTED";
        String link = "/usr/member/myPage";
        int adminId = rq.getLoginedMemberId();
        notificationService.addNotification(memberId, adminId, type,  title, link);

        // 💡 변경: 리디렉트로 안전하게 이동
        return "redirect:/adm/member/list";
    }


}