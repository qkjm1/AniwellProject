package com.example.RSW.service;

import com.example.RSW.repository.CalendarEventRepository;
import com.example.RSW.vo.CalendarEvent;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    // 감정일지 등록
    public ResultData insert(int memberId, LocalDate eventDate, String title, int petId, String content) {
        int affectedRows = calendarEventRepository.insert(memberId, eventDate, title, petId, content);

        if (affectedRows == 0) {
            return ResultData.from("F-InsertFail", "일기 등록에 실패했습니다.");
        }

        return ResultData.from("S-1", "감정일기가 등록되었습니다.");
    }

    //감정 일지 업데이트
    public ResultData update(int id, LocalDate eventDate, String title, String content) {
        int affectedRows = calendarEventRepository.update(id, eventDate, title,  content);

        if (affectedRows == 0) {
            return ResultData.from("F-InsertFail", "일기 수정에 실패했습니다.");
        }

        return ResultData.from("S-1", "감정일기가 수정되었습니다.");
    }

    //감정 일지 삭제
    public void delete(int id) {
        calendarEventRepository.delete(id);
    }

    //멤버 ID로 등록된 감정일지 가져오기
    public List<CalendarEvent> getEventsByMemberId(int memberId) {
        return calendarEventRepository.findByMemberId(memberId);
    }

    // 펫 ID로 등록된 감정일지 가져오기
    public List<CalendarEvent> getEventsByPetId(int petId) {
        return calendarEventRepository.findByPetId(petId);
    }

    // ID로 등록된 감정일지 가져오기
    public CalendarEvent getEventsById(int id) {
        return calendarEventRepository.getEventsById(id);
    }

    // 등록 날짜와 펫 ID로 등록된 감정일지 가져오기
    public List<CalendarEvent> getEventByDateAndPetId(LocalDate eventDate, int petId) {
        return calendarEventRepository.getEventByDateAndPetId(eventDate,petId);
    }
}

