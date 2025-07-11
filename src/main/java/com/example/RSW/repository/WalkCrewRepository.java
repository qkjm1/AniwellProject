package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;

@Mapper
public interface WalkCrewRepository {

	void insert(WalkCrew walkCrew); // 크루 등록

	WalkCrew findById(@Param("id") int id); // ID로 단일 크루 조회

	List<WalkCrew> findByArea(@Param("area") String area); // 지역별 크루 목록 조회

	List<WalkCrew> findAll(); // 전체 크루 목록 조회

	void update(WalkCrew walkCrew); // 크루 정보 수정

	void deleteById(@Param("id") int id); // 크루 삭제


	void insertMemberToCrew(int memberId, int crewId); // 크루 참가


  List<WalkCrew> getWalkCrews(@Param("memberId") int memberId);

}