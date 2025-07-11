package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;

import com.example.RSW.vo.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberRepository {

	int doJoin(String loginId, String loginPw, String name, String nickname, String cellphone,
			   String email, String address, String authName, int authLevel);

	public Member getMemberById(int id);

	public int getLastInsertId();

	public Member getMemberByLoginId(String loginId);

	public Member getMemberByNameAndEmail(String name, String email);

	public void modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphone,
			String email);

	public void modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphone, String email);

	void withdraw(int id);

    List<Member> findAll();

	void updateAuthLevel(int memberId, int authLevel);

	List<Member> getForPrintMembersWithCert(@Param("searchType") String searchType,
											@Param("searchKeyword") String searchKeyword);

}