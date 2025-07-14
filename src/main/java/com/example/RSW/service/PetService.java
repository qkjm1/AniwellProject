package com.example.RSW.service;

import com.example.RSW.repository.PetRepository;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    // 멤버 ID로 펫 목록 호출
    public List<Pet> getPetsByMemberId(int memberId) {
        return petRepository.getPetsByMemberId(memberId);
    }

    // 펫 수정(사진 있음)
     public ResultData updatePet(int petId, String name, String species, String breed, String gender, String birthDate, double weight, String photo) {
        petRepository.updatePet(petId, name,species,breed,gender,birthDate,weight,photo);
         return ResultData.from("S-1", "애완동물 정보 수정 완료");
     }

     //펫 삭제
     public ResultData deletePet(int id) {
        petRepository.deletePet(id);
         return ResultData.from("S-1", "애완동물 삭제 완료");
     }

    // 펫 등록
    public ResultData insertPet(int memberId, String name, String species, String breed,
                                String gender, String birthDate, double weight, String photo) {

        petRepository.insertPet(memberId, name, species, breed, gender, birthDate, weight, photo);

        // 방금 등록된 pet의 id 가져오기
        int id = petRepository.getLastInsertId();

        return ResultData.from("S-1", "반려동물 등록 성공", "등록 성공 id", id);
    }

    // 펫 사진 없이 수정
    public ResultData updatePetyWithoutPhoto(int petId, String name, String species, String breed, String gender, String birthDate, double weight) {
        petRepository.updatePetWithoutPhoto(petId, name,species,breed,gender,birthDate,weight);
        return ResultData.from("S-1", "애완동물 정보 수정 완료");
    }

    // ID로 펫 가져오기
    public Pet getPetsById(int petId) {
        return petRepository.getPetsById(petId);
    }
}
