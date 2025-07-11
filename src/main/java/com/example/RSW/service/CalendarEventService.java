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

    public ResultData insert(int memberId, LocalDate eventDate, int petId, String content) {
        return calendarEventRepository.insert(memberId, eventDate, petId, content);
    }

    public ResultData update(int id, LocalDate eventDate, String content) {
        return calendarEventRepository.update(id, eventDate, content);
    }

    public void delete(int id) {
        calendarEventRepository.delete(id);
    }

    public List<CalendarEvent> getEventsByMemberId(int memberId) {
        return calendarEventRepository.findByMemberId(memberId);
    }

    public List<CalendarEvent> getEventsByPetId(int petId) {
        return calendarEventRepository.findByPetId(petId);
    }

    public CalendarEvent getEventsById(int id) {
        return calendarEventRepository.getEventsById(id);
    }
}

