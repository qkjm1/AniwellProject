package com.example.RSW.controller;

import com.example.RSW.service.CrewChatMessageService;
import com.example.RSW.vo.CrewChatMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class CrewChatApiController {

    private final CrewChatMessageService chatService;

    public CrewChatApiController(CrewChatMessageService chatService) {
        this.chatService = chatService;
    }

    // 해당 채팅방의 채팅 목록 넘기기
    @GetMapping("/{crewId}/messages")
    public List<CrewChatMessage> getChatMessages(@PathVariable int crewId) {
        return chatService.getMessagesByCrewId(crewId);
    }
}

