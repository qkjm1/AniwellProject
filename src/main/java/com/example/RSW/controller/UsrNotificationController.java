package com.example.RSW.controller;

import com.example.RSW.service.NotificationService;
import com.example.RSW.vo.Notification;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/usr/notifications")
public class UsrNotificationController {

    @Autowired
    private Rq rq;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("")
    public String redirectToList() {
        return "redirect:/usr/notifications/list";
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentNotifications() {

        if (!rq.isLogined()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int memberId = rq.getLoginedMemberId();
        List<Notification> list = notificationService.getRecentNotifications(memberId);
        return ResponseEntity.ok(list);
    }


    // 알림 목록 페이지
    @GetMapping("/list")
    public String showNotificationList(HttpServletRequest request, HttpServletResponse response, Model model) {

        if (!rq.isLogined()) {
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>alert('로그인이 필요합니다.'); window.close();</script>");
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null; // JSP 렌더링 안 하고 종료
        }
        int memberId = rq.getLoginedMemberId();

        List<Notification> notifications = notificationService.getNotificationsByMemberId(memberId);

        // 변환 없이 바로 JSP에 전달
        model.addAttribute("notifications", notifications);

        return "usr/notification/list";
    }


//    개별 알림 읽음 처리

    @PostMapping("/markAsRead")
    @ResponseBody
    public ResultData markAsRead(@RequestParam int notificationId) {

        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }

        int memberId = rq.getLoginedMemberId();

        boolean success = notificationService.markAsRead(memberId, notificationId);

        return success
                ? ResultData.from("S-1", "읽음 처리되었습니다.")
                : ResultData.from("F-1", "읽음 처리 실패 또는 권한 없음.");
    }

    @GetMapping("/unreadCount")
    @ResponseBody
    public ResultData getUnreadCount() {
        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }
        int memberId = rq.getLoginedMemberId();
        int count = notificationService.getUnreadCount(memberId);
        return ResultData.from("S-1", "성공", "count", count);
    }

    //    모든 알림 읽음 처리
    @PostMapping("/markAllAsRead")
    @ResponseBody
    public ResultData markAllAsRead() {
        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }

        notificationService.markAllAsRead(rq.getLoginedMemberId());
        return ResultData.from("S-1", "모든 알림을 읽음 처리했습니다.");
    }


    @GetMapping("/readAndGo")
    public String readAndRedirect(@RequestParam int id) {
        Notification n = notificationService.findById(id);
        if (n != null && rq.isLogined() && rq.getLoginedMemberId() == n.getMemberId()) {
            notificationService.markAsRead(n.getMemberId(), n.getId());
            return "redirect:" + n.getLink();
        }
        return "redirect:/"; // fallback
    }

    @GetMapping("/home")
    public String showHome(Model model) {

        if (rq.isLogined()) {
            boolean hasUnread = notificationService.hasUnread(rq.getLoginedMemberId());
            model.addAttribute("hasUnreadNotification", hasUnread);

        }

        return "usr/home/main";
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResultData delete(@RequestParam int id) {
        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }

        int memberId = rq.getLoginedMemberId();
        notificationService.deleteById(id, memberId);
        return ResultData.from("S-1", "알림이 삭제되었습니다.");
    }

    /**
     * 찜 해제 시, 동일 link+title 알림을 모두 삭제
     */
    @RequestMapping(value = "/deleteByLink", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResultData deleteByLink(@RequestParam String link, @RequestParam String title) {
        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }
        int memberId = rq.getLoginedMemberId();
        boolean success = notificationService.deleteByLinkAndTitle(memberId, link, title);
        return success
                ? ResultData.from("S-1", "이전 알림을 삭제했습니다.")
                : ResultData.from("F-1", "삭제 실패 또는 권한 없음.");
    }

}
