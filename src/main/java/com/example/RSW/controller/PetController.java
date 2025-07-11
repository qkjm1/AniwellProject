package com.example.RSW.controller;

import com.example.RSW.service.*;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    @RequestMapping("/usr/pet/petPage")
    public String showTest(@RequestParam("petId") int petId, Model model) throws Exception{
        Pet pet = petService.getPetsById(petId);
        model.addAttribute("pet", pet);

        List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);

        List<Map<String, Object>> events = new ArrayList<>();
        for (PetVaccination pv : list) {
            // ✅ 1. 접종 이벤트
            Map<String, Object> injEvent = new HashMap<>();
            injEvent.put("id", pv.getId());
            injEvent.put("title", pv.getVaccineName() + " 접종");
            injEvent.put("start", pv.getInjectionDate().toString());
            injEvent.put("color", "#4caf50");

            events.add(injEvent);

            // ✅ 2. 다음 예정 이벤트 (nextDueDate가 null이 아닐 때만)
            if (pv.getNextDueDate() != null) {
                Map<String, Object> nextEvent = new HashMap<>();
                nextEvent.put("id", pv.getId());
                nextEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 다음 예정");
                nextEvent.put("start", pv.getNextDueDate().toString());
                nextEvent.put("color", "#f44336");

                events.add(nextEvent);
            }
        }

        // ✅ JSON으로 변환하여 JSP에 전달
        ObjectMapper objectMapper = new ObjectMapper();
        String eventsJson = objectMapper.writeValueAsString(events);
        model.addAttribute("eventsJson", eventsJson);
        return "/usr/pet/petPage"; // JSP or Thymeleaf 페이지
    }
    @RequestMapping("/usr/pet/list")
    public String showPetList(@RequestParam("memberId") int memberId, Model model) {
        List<Pet> pets = petService.getPetsByMemberId(memberId);
        List<WalkCrew> crews = walkCrewService.getWalkCrews(memberId);

        model.addAttribute("pets", pets);
        model.addAttribute("crews", crews);
        return "usr/pet/list"; // JSP or Thymeleaf 페이지
    }

    @RequestMapping("/usr/pet/join")
    public String showJoin(HttpServletRequest req) {
        return "/usr/pet/join";
    }

    @RequestMapping("/usr/pet/doJoin")
    @ResponseBody
    public String doJoin(HttpServletRequest req,
                         @RequestParam("photo") MultipartFile photo,
                         @RequestParam String name,
                         @RequestParam String species,
                         @RequestParam String breed,
                         @RequestParam String gender,
                         @RequestParam String birthDate,
                         @RequestParam double weight) {

        // 유효성 검사 생략 안 함
        if (Ut.isEmptyOrNull(name)) return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
        if (Ut.isEmptyOrNull(species)) return Ut.jsHistoryBack("F-2", "종을 입력하세요");
        if (Ut.isEmptyOrNull(breed)) return Ut.jsHistoryBack("F-3", "품종을 입력하세요");
        if (Ut.isEmptyOrNull(gender)) return Ut.jsHistoryBack("F-4", "성별을 입력하세요");
        if (Ut.isEmptyOrNull(birthDate)) return Ut.jsHistoryBack("F-5", "생일을 입력하세요");
        if (Ut.isEmptyOrNull(String.valueOf(weight))) return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

        // 1. 파일 저장 처리
        String imagePath = null;
        if (!photo.isEmpty()) {
            String uploadDir = "/Users/e-suul/Desktop/aniwell_uploads"; // 실제 저장 경로
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            File dest = new File(uploadDir, fileName);

            try {
                photo.transferTo(dest);
                imagePath = "/uploads/" + fileName; // DB에 저장할 경로
            } catch (IOException e) {
                e.printStackTrace();
                return Ut.jsHistoryBack("F-7", "사진 업로드 중 오류 발생");
            }
        }

        // 2. 서비스로 전달
        ResultData joinRd = petService.insertPet(
                rq.getLoginedMemberId(),
                name, species, breed, gender, birthDate, weight, imagePath
        );

        int id = rq.getLoginedMemberId();
        return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../pet/list?memberId=" + id);
    }

    @RequestMapping("/usr/pet/modify")
    public String showModify(@RequestParam("petId") int petId, Model model) {
        Pet pet = petService.getPetsById(petId);

        model.addAttribute("pet", pet);
        return "usr/pet/modify";
    }

    @RequestMapping("/usr/pet/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, @RequestParam("petId") int petId, String name, String species, String breed,
                           String gender, String birthDate, double weight, MultipartFile photo) {

        // 비번은 안바꾸는거 가능(사용자) 비번 null 체크는 x

        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
        }
        if (Ut.isEmptyOrNull(species)) {
            return Ut.jsHistoryBack("F-2", "종을 입력하세요");

        }
        if (Ut.isEmptyOrNull(breed)) {
            return Ut.jsHistoryBack("F-3", "중성화여부를 입력하세요");

        }
        if (Ut.isEmptyOrNull(gender)) {
            return Ut.jsHistoryBack("F-4", "성별을 입력하세요");

        }
        if (Ut.isEmptyOrNull(birthDate)) {
            return Ut.jsHistoryBack("F-5", "생일을 입력하세요");

        }
        if (Ut.isEmptyOrNull(String.valueOf(weight))) {
            return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

        }

        String photoPath = null;

        if (photo != null && !photo.isEmpty()) {
            try {
                String uploadDir = "/Users/e-suul/Desktop/AniwellProject/src/main/resources/static/img/pet/";
                String newFilename = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + newFilename);

                Files.createDirectories(filePath.getParent()); // 폴더 없으면 생성
                photo.transferTo(filePath.toFile());

                photoPath = "/img/pet/" + newFilename; // DB에는 상대경로만 저장

            } catch (Exception e) {
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


    @RequestMapping("/usr/pet/analysis")
    public String showAnalysisForm() {
        return "usr/pet/emotion";  // 분석 요청 form (이미지 경로 선택)
    }

    @RequestMapping("/usr/pet/gallery")
    public String showGallery(@RequestParam("petId") int petId, Model model) {
        List<PetAnalysis> analysisList = petAnalysisService.getAnalysisByPetId(petId);

        model.addAttribute("analysisList", analysisList);
        return "usr/pet/gallery";  // 분석 요청 form (이미지 경로 선택)
    }

    @PostMapping("/usr/pet/analysis/do")
    @ResponseBody
    public Map<String, Object> doAnalysis(
            @RequestParam("petId") int petId,
            @RequestParam("species") String species,
            @RequestParam("imageFile") MultipartFile imageFile) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 이미지 저장
            String saveDir = "/Users/e-suul/Desktop/aniwell_uploads/";
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            File savedFile = new File(saveDir + fileName);
            imageFile.transferTo(savedFile);

            // 2. 종에 따라 파이썬 실행 파일 선택
            String scriptPath;
            if ("강아지".equals(species)) {
                scriptPath = "/Users/e-suul/Desktop/ESeul-main/dog_test.py";
            } else {
                scriptPath = "/Users/e-suul/Desktop/ESeul-main/cat_test.py";
            }

            // 3. 파이썬 실행
            String command = "python3 " + scriptPath + " " + savedFile.getAbsolutePath();
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

            // 4. JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(lastLine);
            String emotion = root.get("emotion").asText();
            double confidence = root.get("probabilities").get(emotion).asDouble();

            // 5. DB 저장
            PetAnalysis analysis = new PetAnalysis();
            analysis.setPetId(petId);
            analysis.setImagePath("/uploads/" + fileName);
            analysis.setEmotionResult(emotion);
            analysis.setConfidence(confidence);
            petAnalysisService.save(analysis);

            // 6. 응답 반환
            result.put("emotionResult", emotion);
            result.put("confidence", String.format("%.2f", confidence));
            result.put("imagePath", "/uploads/" + fileName);

            // 🔥 감정별 확률 map 추가
            Map<String, Double> probabilities = new HashMap<>();
            root.get("probabilities").fields().forEachRemaining(entry -> {
                probabilities.put(entry.getKey(), entry.getValue().asDouble());
            });
            result.put("probabilities", probabilities);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("emotionResult", "error");
            result.put("confidence", "0");
            result.put("imagePath", "");
        }

        return result;
    }





    @ResponseBody
    @RequestMapping("/usr/pet/delete")
    public String doDelete(HttpServletRequest req, @RequestParam("petId") int petId) {

        ResultData deleteRd = petService.deletePet(petId);
        int id = rq.getLoginedMemberId();
        return Ut.jsReplace(deleteRd.getResultCode(), deleteRd.getMsg(), "../pet/list?memberId="+id); // JSP 경로
    }

    @RequestMapping("/usr/pet/vaccination/registration")
    public String showRegistration(HttpServletRequest req,@RequestParam("petId") int petId) {
        return "/usr/pet/vaccinationRegistration";
    }

    @RequestMapping("/usr/pet/vaccination/doRegistration")
    @ResponseBody
    public ResultData doRegistration(HttpServletRequest req,
                                     @RequestParam("petId") int petId,
                                     String vaccineName,
                                     String injectionDate, String notes) {

        if (Ut.isEmptyOrNull(vaccineName)) {
            return ResultData.from("F-2", "백신 이름을 입력하세요");
        }
        if (Ut.isEmptyOrNull(injectionDate)) {
            return ResultData.from("F-3", "접종 날짜를 입력하세요");
        }

        if (notes == null) {
            return petVaccinationService.insertPetVaccination(petId, vaccineName, injectionDate);
        } else {
            return petVaccinationService.insertPetVaccinationWithNotes(petId, vaccineName, injectionDate,notes);
        }
    }



    @RequestMapping("/usr/pet/vaccination/modify")
    public String showVaccinationModify(@RequestParam("vaccinationId") int vaccinationId, Model model) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        model.addAttribute("petVaccination", petVaccination);
        return "usr/pet/vaccinationModify";
    }

    @RequestMapping("/usr/pet/vaccination/doModify")
    @ResponseBody
    public String doVaccinationModify(@RequestParam("vaccinationId") int vaccinationId, String vaccineName, String injectionDate, String notes) {


        if (Ut.isEmptyOrNull(vaccineName)) {
            return Ut.jsHistoryBack("F-1", "백신명을 입력하세요");
        }
        if (Ut.isEmptyOrNull(injectionDate)) {
            return Ut.jsHistoryBack("F-2", "접종일자를 입력하세요");

        }
        if (Ut.isEmptyOrNull(injectionDate)) {
            return Ut.jsHistoryBack("F-2", "다음일자를 입력하세요");

        }

        ResultData modifyRd;
        if (notes == null) {
            modifyRd = petVaccinationService.updatePetVaccination( vaccinationId, vaccineName,injectionDate);
        } else {
            modifyRd = petVaccinationService.updatePetVaccinationWithNotes( vaccinationId, vaccineName,injectionDate,notes);
        }

        int id = petVaccinationService.getPetIdById(vaccinationId);
        return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../vaccination?petId="+id);
    }

    @RequestMapping("/usr/pet/vaccination/detail")
    public String showVaccinationDetail(@RequestParam("vaccinationId") int vaccinationId, Model model) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        model.addAttribute("petVaccination", petVaccination);
        return "usr/pet/vaccinationDetail";  // 상세보기 JSP 페이지
    }

    @ResponseBody
    @RequestMapping("/usr/pet/vaccination/delete")
    public String doVaccinationDelete(@RequestParam("vaccinationId") int  vaccinationId) {
        int id = petVaccinationService.getPetIdById(vaccinationId);
        ResultData deleteRd = petVaccinationService.deletePetVaccination(vaccinationId);
        return "jsReplace('/usr/pet/vaccination?petId=" + id + "', '삭제가 완료되었습니다.');";
    }

    @RequestMapping("/usr/pet/daily")
    public String showDaily(@RequestParam("petId") int petId, Model model) {
        List<CalendarEvent> events = calendarEventService.getEventsByPetId(petId);
        model.addAttribute("events", events);
        model.addAttribute("petId", petId); // 👉 jsp에서 다시 요청 시 필요
        return "usr/pet/daily";  // 상세보기 jsp
    }

    @RequestMapping("/usr/pet/daily/write")
    @ResponseBody
    public String addEvent(@RequestParam("petId") int petId,
                           @RequestParam("eventDate") String eventDateStr,
                           @RequestParam("content") String content) {

        if (Ut.isEmptyOrNull(content)) {
            return Ut.jsHistoryBack("F-1", "내용을 입력하세요");
        }

        if (Ut.isEmptyOrNull(eventDateStr)) {
            return Ut.jsHistoryBack("F-1", "날짜를 입력하세요");
        }

        LocalDate eventDate = LocalDate.parse(eventDateStr); // 문자열 → 날짜

        ResultData doWriteRd = calendarEventService.insert(rq.getLoginedMemberId(), eventDate, petId, content);

        return Ut.jsReplace(doWriteRd.getResultCode(), doWriteRd.getMsg(), "/usr/pet/daily?petId=" + petId);
    }

    @RequestMapping("/usr/pet/daily/domodify")
    @ResponseBody
    public String updateEvent(@RequestParam("id") int id,
                              @RequestParam("eventDate") String eventDateStr,
                              @RequestParam("content") String content) {

        if (Ut.isEmptyOrNull(content)) {
            return Ut.jsHistoryBack("F-1", "내용을 입력하세요");
        }

        CalendarEvent calendarEvent = calendarEventService.getEventsById(id);
        if (calendarEvent == null) {
            return Ut.jsHistoryBack("F-1", "해당 게시글은 존재하지 않습니다.");
        }

        LocalDate eventDate = LocalDate.parse(eventDateStr);

        ResultData doModifyRd = calendarEventService.update(id, eventDate, content);

        return Ut.jsReplace(doModifyRd.getResultCode(), doModifyRd.getMsg(), "/usr/pet/daily?petId=" + calendarEvent.getPetId());
    }

    @RequestMapping("/usr/pet/daily/delete")
    @ResponseBody
    public String deleteEvent(@RequestParam("id") int id) {
        CalendarEvent calendarEvent = calendarEventService.getEventsById(id);
        if (calendarEvent == null) {
            return Ut.jsHistoryBack("F-1", "해당 게시글은 존재하지 않습니다.");
        }

        calendarEventService.delete(id);

        return Ut.jsReplace("S-1", "삭제가 완료되었습니다.", "/usr/pet/daily?petId=" + calendarEvent.getPetId());
    }


}
