
package com.example.RSW.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.service.DistrictService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.Date;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/usr/walkCrew")
public class UsrWalkCrewController {
	
	@Autowired
	private DistrictService districtService;

	@Autowired
	private DistrictRepository districtRepository;

	@Autowired
	private WalkCrewMemberService walkCrewMemberService;

	private final WalkCrewService walkCrewService;

	// ✅ AppConfig에서 Kakao Key 가져오기 위한 DI
	@Autowired
	private AppConfig appConfig; // @Value 주입된 클래스
	
	@Autowired
	public UsrWalkCrewController(WalkCrewService walkCrewService) {
		this.walkCrewService = walkCrewService;
	}

	// 크루 목록 페이지 이동 (예: /usr/walkCrew/list)
	@GetMapping("/list")
	public String showCrewList(HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq"); // 필터 또는 인터셉터에서 세팅된 Rq
		model.addAttribute("rq", rq); // JSP에서 사용 가능하게 전달

		List<WalkCrew> crews = walkCrewService.getAllCrews();// 전체 크루 리스트 조회
		model.addAttribute("crews", crews);
		model.addAttribute("kakaoJsKey", appConfig.getKakaoRestApiKey()); // 리스트에서 사용할 카카오apikey REST용 써야
		return "usr/walkCrew/list";
	}
	
	@GetMapping("/api/list")
	@ResponseBody
	public ResultData showCrewList(HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq"); // 필터 또는 인터셉터에서 세팅된 Rq

		List<WalkCrew> crews = walkCrewService.getAllCrews();// 전체 크루 리스트 조회
		
		return ResultData.from("S-1", "crewlist", "crews", crews);
	}

	// ✅ 크루 등록 폼 페이지 출력
	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("kakaoJsKey", appConfig.getKakaoJavascriptKey()); // JSP에서 사용될 키
		return "usr/walkCrew/create";
	}

	// 크루 등록 처리
	@PostMapping("/doCreate")
	public String doCreate(WalkCrew walkCrew, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (!rq.isLogined()) {
			return "redirect:/usr/member/login?msg=로그인 후 이용해주세요.";
		}

		// ✅ 디버깅용 로그 출력
		System.out.println("city = " + walkCrew.getCity());
		System.out.println("district = " + walkCrew.getDistrict());
		System.out.println("dong = " + walkCrew.getDong());

		walkCrew.setLeaderId(rq.getLoginedMemberId()); // ✅ 로그인된 사용자 ID 주입
		walkCrewService.createCrew(walkCrew);// 서비스 호출하여 DB에 저장

		return "redirect:/usr/walkCrew/list";
	}

	// 크루 상세보기 페이지

	@GetMapping("/detail/{id}")
	public String showDetail(@PathVariable int id, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		WalkCrew crew = walkCrewService.getCrewById(id);

		// ✅ createdAt → Date 변환
		Date createdDate = Date.from(crew.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());

		// ✅ 지역 이름 조회
		String crewLocation = "";
		if (crew.getDistrictId() != 0) {
			District district = districtService.findById(crew.getDistrictId());
			if (district != null) {
				crewLocation = district.getSido() + " " + district.getSigungu() + " " + district.getDong();
			}
		}

		// ✅ 크루 가입 여부 체크 후 JSP로 넘기기
		if (rq.isLogined()) {
			boolean isJoined = walkCrewMemberService.isJoinedCrew(rq.getLoginedMemberId(), crew.getId());
			model.addAttribute("isJoined", isJoined);
		}

		model.addAttribute("crew", crew);
		model.addAttribute("createdDate", createdDate);
		model.addAttribute("crewLocation", crewLocation);

		return "usr/walkCrew/detail";
	}

	// ✅ 크루 참가 처리
	@PostMapping("/join")
	public String joinCrew(@RequestParam("crewId") int crewId, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (!rq.isLogined()) {
			String encodedMsg = URLEncoder.encode("로그인 후 이용해주세요.", StandardCharsets.UTF_8);
			return "redirect:/usr/member/login?msg=" + encodedMsg;
		}

		int memberId = rq.getLoginedMemberId();

		// 이미 참가했는지 확인
		if (!walkCrewService.hasAlreadyJoined(crewId, memberId)) {
			walkCrewService.addMemberToCrew(crewId, memberId);
			String encodedMsg = URLEncoder.encode("참가 신청이 완료되었습니다.", StandardCharsets.UTF_8);
			return "redirect:/usr/walkCrew/detail/" + crewId + "?msg=" + encodedMsg;
		} else {
			String encodedMsg = URLEncoder.encode("이미 참가한 크루입니다.", StandardCharsets.UTF_8);
			return "redirect:/usr/walkCrew/detail/" + crewId + "?msg=" + encodedMsg;
		}
	}

	// ✅ 특정 시, 구에 해당하는 동 목록 반환 (Ajax)
	@GetMapping("/getDongs")
	@ResponseBody
	public List<String> getDongs(@RequestParam String city, @RequestParam String district) {
		return districtService.findDongsByCityAndDistrict(city, district);// 동 리스트 반환
	}

	// ✅ 선택된 시/구/동에 해당하는 districtId 반환 (Ajax)
	@GetMapping("/getDistrictId")
	@ResponseBody
	public String getDistrictId(@RequestParam String city, @RequestParam String district, @RequestParam String dong) {
		int id = districtRepository.getDistrictIdByFullAddress(city, district, dong);
		return String.valueOf(id);// 정수 → 문자열 변환 후 반환
	}

	// 참가 요청 권한
	@PostMapping("/approveApplicant")
	@ResponseBody
	public ResultData approveApplicant(@RequestParam int crewId, @RequestParam int memberId) {
		walkCrewService.approveMember(crewId, memberId);
		return ResultData.from("S-1", "참가 요청을 수락했습니다.");
	}

}