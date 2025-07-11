package com.example.RSW.repository;

import com.example.RSW.vo.CalendarEvent;
import com.example.RSW.vo.ResultData;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CalendarEventRepository {
    ResultData insert(int memberId, LocalDate eventDate, int petId, String content);
    ResultData update(int id, LocalDate eventDate, String content);
    void delete(int id);
    List<CalendarEvent> findByMemberId(int memberId);
    List<CalendarEvent> findByPetId(int petId);

    CalendarEvent getEventsById(int id);
}
