package com.example.RSW.service;

import com.example.RSW.repository.PetAnalysisRepository;
import com.example.RSW.vo.PetAnalysis;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PetAnalysisService {

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    public void saveAnalysis(PetAnalysis analysis) {

    }


    public List<PetAnalysis> getAnalysisByPetId(int petId) {
        return petAnalysisRepository.getAnalysisByPetId(petId);
    }

    public void save(PetAnalysis analysis) {
        petAnalysisRepository.insertAnalysis(analysis);
    }
}
