package com.example.RSW.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.BoardRepository;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.WalkCrewRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;

@Service
public class WalkCrewService {

	@Autowired
	private DistrictRepository districtRepository;

	private final WalkCrewRepository walkCrewRepository;

	public WalkCrewService(WalkCrewRepository walkCrewRepository) {
		this.walkCrewRepository = walkCrewRepository;
	}

	// 크루 등록
	public void createCrew(WalkCrew walkCrew) {
		walkCrewRepository.insert(walkCrew);
	}

	// ID로 크루 상세 조회
	public WalkCrew getCrewById(int id) {
		return walkCrewRepository.findById(id);
	}

	// 지역별 크루 목록 조회
	public List<WalkCrew> getCrewsByArea(String area) {
		return walkCrewRepository.findByArea(area);
	}

	// 전체 크루 목록 조회
	public List<WalkCrew> getAllCrews() {
		return walkCrewRepository.findAll();
	}

	// 크루 수정
	public void updateCrew(WalkCrew walkCrew) {
		walkCrewRepository.update(walkCrew);
	}

	// 크루 삭제
	public void deleteCrew(int id) {
		walkCrewRepository.deleteById(id);
	}


	public void joinCrew(int memberId, int crewId) {
		walkCrewRepository.insertMemberToCrew(memberId, crewId);
	}


	public List<WalkCrew> getWalkCrews(int memberId) { return walkCrewRepository.getWalkCrews(memberId);}

}