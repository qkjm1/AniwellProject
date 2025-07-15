package com.example.RSW.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.service.*;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@Controller
public class PetController {

	@Autowired
	Rq rq;

	@Autowired
	private PetService petService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private PetVaccinationService petVaccinationService;

	@Autowired
	private PetAnalysisService petAnalysisService;

	@Autowired
	private CalendarEventService calendarEventService;

	@Autowired
	private Cloudinary cloudinary;

	// 테스트용
	@RequestMapping("/usr/pet/test")
	public String test() {
		return "usr/pet/test";
	}

	// 주변 펫 샵 조회
	@RequestMapping("/usr/pet/petPlace")
	public String showTest() {
		return "usr/pet/petPlace";
	}

	// 펫 상세페이지
	@RequestMapping("/usr/pet/petPage")
	public String showTest(@RequestParam("petId") int petId, Model model) throws Exception {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}
		model.addAttribute("pet", pet);

		List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);

		List<Map<String, Object>> events = new ArrayList<>();
		for (PetVaccination pv : list) {
			// 접종 이벤트
			Map<String, Object> injEvent = new HashMap<>();
			injEvent.put("id", pv.getId());
			injEvent.put("title", pv.getVaccineName() + " 접종");
			injEvent.put("start", pv.getInjectionDate().toString());
			injEvent.put("color", "#4caf50"); // 캘린더 표시 색 (변경 가능)

			events.add(injEvent); // 접종한 백신 데이터

			// 다음 예정 이벤트 (이름이 같은 백신 들어오면 마지막 접종의 다음 날짜만 표시)
			if (pv.getNextDueDate() != null) {
				Map<String, Object> nextEvent = new HashMap<>();
				nextEvent.put("id", pv.getId());
				nextEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 다음 예정");
				nextEvent.put("start", pv.getNextDueDate().toString());
				nextEvent.put("color", "#f44336"); // 캘린더 표시 색 (변경 가능)

				events.add(nextEvent); // 최종적인 다음 날짜
			}
		}

		ObjectMapper objectMapper = new ObjectMapper();
		String eventsJson = objectMapper.writeValueAsString(events);
		model.addAttribute("eventsJson", eventsJson); // 접종이벤트 내용 넘김
		return "usr/pet/petPage"; // JSP or Thymeleaf 페이지
	}

	// 등록한 펫 목록 / 가입한 크루 목록
	@RequestMapping("/usr/pet/list")
	public String showPetList(@RequestParam("memberId") int memberId, Model model) {
		int loginId = rq.getLoginedMemberId();
		if (loginId != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}
		List<Pet> pets = petService.getPetsByMemberId(memberId);
		List<WalkCrew> crews = walkCrewService.getWalkCrews(memberId);

		Member loginesMember = rq.getLoginedMember();

		model.addAttribute("member", loginesMember); // 로그인 멤버
		model.addAttribute("pets", pets); // 해당 멤버가 등록한 펫ID
		model.addAttribute("crews", crews); // 해당 멤버가 가입한 크루목록
		return "usr/pet/list"; // JSP or Thymeleaf 페이지
	}

	// 펫등록 페이지 이동
	@RequestMapping("/usr/pet/join")
	public String showJoin(HttpServletRequest req) {
		return "/usr/pet/join";
	}

	// 펫 등록 로직

	@RequestMapping("/usr/pet/doJoin")
	@ResponseBody
	public String doJoin(HttpServletRequest req, @RequestParam("photo") MultipartFile photo, @RequestParam String name,
			@RequestParam String species, @RequestParam String breed, @RequestParam String gender,
			@RequestParam String birthDate, @RequestParam double weight) {

		if (Ut.isEmptyOrNull(name))
			return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
		if (Ut.isEmptyOrNull(species))
			return Ut.jsHistoryBack("F-2", "종을 입력하세요");
		if (Ut.isEmptyOrNull(breed))
			return Ut.jsHistoryBack("F-3", "품종을 입력하세요");
		if (Ut.isEmptyOrNull(gender))
			return Ut.jsHistoryBack("F-4", "성별을 입력하세요");
		if (Ut.isEmptyOrNull(birthDate))
			return Ut.jsHistoryBack("F-5", "생일을 입력하세요");
		if (Ut.isEmptyOrNull(String.valueOf(weight)))
			return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

		String imagePath = null;
		if (!photo.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
				imagePath = (String) uploadResult.get("secure_url");
			} catch (IOException e) {
				e.printStackTrace();
				return Ut.jsHistoryBack("F-7", "사진 업로드 중 오류 발생");
			}
		}

		ResultData joinRd = petService.insertPet(rq.getLoginedMemberId(), name, species, breed, gender, birthDate,
				weight, imagePath);

		int id = rq.getLoginedMemberId();
		return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../pet/list?memberId=" + id);
	}

	// 펫 정보 수정 페이지로 이동

	@RequestMapping("/usr/pet/modify")
	public String showModify(@RequestParam("petId") int petId, Model model) {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}

		model.addAttribute("pet", pet);
		return "usr/pet/modify";
	}

	// 펫 정보 수정 로직
	@RequestMapping("/usr/pet/doModify")
	@ResponseBody
	public String doModify(HttpServletRequest req, @RequestParam("petId") int petId, String name, String species,
			String breed, String gender, String birthDate, double weight, MultipartFile photo) {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-0", "권한이 없습니다.");
		}

		if (Ut.isEmptyOrNull(name))
			return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
		if (Ut.isEmptyOrNull(species))
			return Ut.jsHistoryBack("F-2", "종을 입력하세요");
		if (Ut.isEmptyOrNull(breed))
			return Ut.jsHistoryBack("F-3", "품종을 입력하세요");
		if (Ut.isEmptyOrNull(gender))
			return Ut.jsHistoryBack("F-4", "성별을 입력하세요");
		if (Ut.isEmptyOrNull(birthDate))
			return Ut.jsHistoryBack("F-5", "생일을 입력하세요");
		if (Ut.isEmptyOrNull(String.valueOf(weight)))
			return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

		String photoPath = null;
		if (photo != null && !photo.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(photo.getBytes(), ObjectUtils.emptyMap());
				photoPath = (String) uploadResult.get("secure_url");
			} catch (IOException e) {
				e.printStackTrace();
				return Ut.jsHistoryBack("F-1", "사진 업로드 실패");
			}
		}

		ResultData modifyRd;
		if (photoPath == null) {
			modifyRd = petService.updatePetyWithoutPhoto(petId, name, species, breed, gender, birthDate, weight);
		} else {
			modifyRd = petService.updatePet(petId, name, species, breed, gender, birthDate, weight, photoPath);
		}

		int id = rq.getLoginedMemberId();
		return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../pet/list?memberId=" + id);
	}

	// 감정 분석 페이지 이동
	@RequestMapping("/usr/pet/analysis")
	public String showAnalysisForm() {
		return "usr/pet/emotion"; // 분석 요청 form (이미지 경로 선택)
	}

	// 감정 갤러리 이동
	@RequestMapping("/usr/pet/gallery")
	public String showGallery(@RequestParam("petId") int petId, Model model) {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}
		List<PetAnalysis> analysisList = petAnalysisService.getAnalysisByPetId(petId);

		// 감정 분석 사진 목록 리스트
		model.addAttribute("analysisList", analysisList);
		return "usr/pet/gallery"; // 분석 요청 form (이미지 경로 선택)
	}

	// 감정 분석 로직
	@PostMapping("/usr/pet/analysis/do")
	@ResponseBody
	public Map<String, Object> doAnalysis(@RequestParam("petId") int petId, @RequestParam("species") String species,
			@RequestParam("imageFile") MultipartFile imageFile) {

		Map<String, Object> result = new HashMap<>();
		try {
			// 1. Cloudinary 업로드
			Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
			String imageUrl = (String) uploadResult.get("secure_url");

			// 2. 임시 파일로 저장해서 파이썬에 전달
			File tempFile = File.createTempFile("emotion_", ".jpg");
			imageFile.transferTo(tempFile);

			// 3. 종에 따라 파이썬 파일 선택
			String scriptPath;
			if ("강아지".equals(species)) {
				scriptPath = "/Users/e-suul/Desktop/ESeul-main/dog_test.py";
			} else {
				scriptPath = "/Users/e-suul/Desktop/ESeul-main/cat_test.py";
			}

			// 4. 파이썬 실행
			String command = "python3 " + scriptPath + " " + tempFile.getAbsolutePath();
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
			String line;
			String lastLine = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("🐍 Python output: " + line);
				lastLine = line;
			}

			process.waitFor();
			System.out.println("✅ 파이썬 종료 코드: " + process.exitValue());
			System.out.println("⚠ 최종 파이썬 결과 문자열: " + lastLine);

			if (lastLine == null || !lastLine.trim().startsWith("{")) {
				throw new RuntimeException("❌ 파이썬 실행 실패 또는 JSON 형식 아님");
			}

			// 5. JSON 파싱
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(lastLine);
			String emotion = root.get("emotion").asText();
			double confidence = root.get("probabilities").get(emotion).asDouble();

			// 6. DB 저장
			PetAnalysis analysis = new PetAnalysis();
			analysis.setPetId(petId);
			analysis.setImagePath(imageUrl); // Cloudinary URL 저장
			analysis.setEmotionResult(emotion);
			analysis.setConfidence(confidence);
			petAnalysisService.save(analysis);

			// 7. 응답 반환
			result.put("emotionResult", emotion); // 감정 결과
			result.put("confidence", String.format("%.2f", confidence)); // 감정 %
			result.put("imagePath", imageUrl); // 이미지

			Map<String, Double> probabilities = new HashMap<>();
			root.get("probabilities").fields().forEachRemaining(entry -> {
				probabilities.put(entry.getKey(), entry.getValue().asDouble());
			});
			result.put("probabilities", probabilities);

			// 임시 파일 삭제
			tempFile.delete();

		} catch (Exception e) {
			e.printStackTrace();
			result.put("emotionResult", "error");
			result.put("confidence", "0");
			result.put("imagePath", "");
		}

		return result;
	}

	// 펫 삭제 로직
	@ResponseBody
	@RequestMapping("/usr/pet/delete")
	public String doDelete(HttpServletRequest req, @RequestParam("petId") int petId) {
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}

		ResultData deleteRd = petService.deletePet(petId);
		int id = rq.getLoginedMemberId();
		return Ut.jsReplace(deleteRd.getResultCode(), deleteRd.getMsg(), "../pet/list?memberId=" + id); // JSP 경로
	}

	// 백신 등록 페이지 이동
	@RequestMapping("/usr/pet/vaccination/registration")
	public String showRegistration(HttpServletRequest req, @RequestParam("petId") int petId) {
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}
		return "/usr/pet/vaccinationRegistration";
	}

	// 백신 등록 로직
	@RequestMapping("/usr/pet/vaccination/doRegistration")
	@ResponseBody
	public ResultData doRegistration(HttpServletRequest req, @RequestParam("petId") int petId, String vaccineName,
			String injectionDate, String notes) {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return ResultData.from("F-0", "권한이 없습니다.");
		}

		if (Ut.isEmptyOrNull(vaccineName)) {
			return ResultData.from("F-2", "백신 이름을 입력하세요");
		}
		if (Ut.isEmptyOrNull(injectionDate)) {
			return ResultData.from("F-3", "접종 날짜를 입력하세요");
		}

		// 비고 있는 경우와 없는 경우 로직 분리
		if (notes == null) {
			return petVaccinationService.insertPetVaccination(petId, vaccineName, injectionDate);
		} else {
			return petVaccinationService.insertPetVaccinationWithNotes(petId, vaccineName, injectionDate, notes);
		}
	}

	// 백신 수정 페이지 이동
	@RequestMapping("/usr/pet/vaccination/modify")
	public String showVaccinationModify(@RequestParam("vaccinationId") int vaccinationId, Model model) {
		PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
		int petId = petVaccination.getPetId();
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-0", "권한이 없습니다.");
		}
		model.addAttribute("petVaccination", petVaccination); // 해당 id에 해당하는 백신
		return "usr/pet/vaccinationModify";
	}

	// 백신 수정 로직
	@RequestMapping("/usr/pet/vaccination/doModify")
	@ResponseBody
	public ResultData doVaccinationModify(@RequestParam("vaccinationId") int vaccinationId,
			@RequestParam String vaccineName, @RequestParam String injectionDate,
			@RequestParam(required = false) String notes) {

		PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
		int petId = petVaccination.getPetId();
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);

		if (pet.getMemberId() != memberId) {
			return ResultData.from("F-0", "권한이 없습니다.");
		}

		if (Ut.isEmptyOrNull(vaccineName)) {
			return ResultData.from("F-1", "백신명을 입력하세요");
		}

		if (Ut.isEmptyOrNull(injectionDate)) {
			return ResultData.from("F-2", "접종일자를 입력하세요");
		}

		ResultData modifyRd;
		if (Ut.isEmptyOrNull(notes)) {
			modifyRd = petVaccinationService.updatePetVaccination(vaccinationId, vaccineName, injectionDate);
		} else {
			modifyRd = petVaccinationService.updatePetVaccinationWithNotes(vaccinationId, vaccineName, injectionDate,
					notes);
		}

		return ResultData.from("S-1", "수정 완료");
	}

	// 백신 정보 상세보기
	@RequestMapping("/usr/pet/vaccination/detail")
	public String showVaccinationDetail(@RequestParam("vaccinationId") int vaccinationId, Model model) {
		PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
		int petId = petVaccination.getPetId();
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-0", "권한이 없습니다.");
		}

		model.addAttribute("petVaccination", petVaccination); // 해당 id에 해당하는 백신
		return "usr/pet/vaccinationDetail";
	}

	// 벡신 지우는 로직
	@ResponseBody
	@RequestMapping("/usr/pet/vaccination/delete")
	public String doVaccinationDelete(@RequestParam("vaccinationId") int vaccinationId) {

		PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
		int petId = petVaccination.getPetId();
		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-0", "권한이 없습니다.");
		}
		petVaccinationService.deletePetVaccination(vaccinationId);
		return "jsReplace('/usr/pet/vaccination?petId=" + petId + "', '삭제가 완료되었습니다.');";
	}

	// 펫 감정일기 페이지 이동
	@RequestMapping("/usr/pet/daily")
	public String showDaily(@RequestParam("petId") int petId, Model model) {

		int memberId = rq.getLoginedMemberId();
		Pet pet = petService.getPetsById(petId);
		if (pet.getMemberId() != memberId) {
			return Ut.jsHistoryBack("F-1", "권한이 없습니다.");
		}
		List<CalendarEvent> events = calendarEventService.getEventsByPetId(petId);
		model.addAttribute("events", events); // 감정일기에 등록된 이벤트들
		model.addAttribute("petId", petId); // 해당 펫의 ID
		return "usr/pet/daily";
	}

	// 펫 감정일지 등록 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/write")
	@ResponseBody
	public Map<String, Object> addEvent(@RequestParam("petId") int petId,
			@RequestParam("eventDate") String eventDateStr, @RequestParam("title") String title,
			@RequestParam("content") String content, HttpServletRequest req) {

		Map<String, Object> result = new HashMap<>();

		try {
			// 유효성 검사
			if (Ut.isEmptyOrNull(title)) {
				result.put("resultCode", "F-1");
				result.put("msg", "감정을 선택하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(content)) {
				result.put("resultCode", "F-2");
				result.put("msg", "내용을 입력하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(eventDateStr)) {
				result.put("resultCode", "F-3");
				result.put("msg", "날짜를 선택하세요");
				return result;
			}

			// 날짜 파싱
			LocalDate eventDate = LocalDate.parse(eventDateStr);

			// petId로 memberId 추출
			Pet pet = petService.getPetsById(petId);
			if (pet == null) {
				result.put("resultCode", "F-4");
				result.put("msg", "해당 반려동물을 찾을 수 없습니다.");
				return result;
			}

			int loginedMemberId = pet.getMemberId();

			// DB 저장
			ResultData doWriteRd = calendarEventService.insert(loginedMemberId, eventDate, title, petId, content);

			result.put("resultCode", doWriteRd.getResultCode()); // S- 또는 F-
			result.put("msg", doWriteRd.getMsg()); // 오류 메세지 및 성공 메세지
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			result.put("resultCode", "F-500");
			result.put("msg", "서버 오류: " + e.getMessage());
			return result;
		}
	}

	// 감정일지 수정 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/domodify")
	@ResponseBody
	public Map<String, Object> updateEvent(@RequestParam("id") int id, @RequestParam("eventDate") String eventDateStr,
			@RequestParam("title") String title, @RequestParam("content") String content) {

		Map<String, Object> result = new HashMap<>();

		try {
			// 유효성 검사
			if (Ut.isEmptyOrNull(title)) {
				result.put("resultCode", "F-1");
				result.put("msg", "감정을 선택하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(content)) {
				result.put("resultCode", "F-2");
				result.put("msg", "내용을 입력하세요");
				return result;
			}

			if (Ut.isEmptyOrNull(eventDateStr)) {
				result.put("resultCode", "F-3");
				result.put("msg", "날짜를 선택하세요");
				return result;
			}

			// 날짜 파싱
			LocalDate eventDate = LocalDate.parse(eventDateStr);

			// DB 저장
			ResultData doWriteRd = calendarEventService.update(id, eventDate, title, content);

			result.put("resultCode", doWriteRd.getResultCode()); // S- 또는 F-
			result.put("msg", doWriteRd.getMsg()); // 오류 메세지 및 성공 메세지
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			result.put("resultCode", "F-500");
			result.put("msg", "서버 오류: " + e.getMessage());
			return result;
		}
	}

	// 감정일지 삭제 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/delete")
	@ResponseBody
	public Map<String, Object> deleteEvent(@RequestParam("id") int id) {
		Map<String, Object> result = new HashMap<>();

		CalendarEvent calendarEvent = calendarEventService.getEventsById(id);
		if (calendarEvent == null) {
			result.put("resultCode", "F-1");
			result.put("msg", "해당 일기를 찾을 수 없습니다.");
			return result;
		}

		calendarEventService.delete(id);

		result.put("resultCode", "S-1");
		result.put("msg", "삭제가 완료되었습니다.");
		result.put("petId", calendarEvent.getPetId());
		return result;
	}

	// 일 기 상세 보기 로직(json 결과 출력)
	@RequestMapping("/usr/pet/daily/detail")
	@ResponseBody
	public Map<String, Object> detailEvent(@RequestParam("id") int id) {
		CalendarEvent calendarEvent = calendarEventService.getEventsById(id);

		if (calendarEvent == null) {
			return Map.of("resultCode", "F-1", "msg", "해당 일기를 찾을 수 없습니다.");
		}

		return Map.of("resultCode", "S-1", "calendarEvent", calendarEvent);
	}

}

