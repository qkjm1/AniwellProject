package com.example.RSW.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.MemberRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;

import java.util.List;

@Service
public class MemberService {

	@Value("${custom.siteMainUri}")
	private String siteMainUri;
	@Value("${custom.siteName}")
	private String siteName;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MailService mailService;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public ResultData notifyTempLoginPwByEmail(Member actor) {
		String title = "[" + siteName + "] 임시 패스워드 발송";
		String tempPassword = Ut.getTempPassword(6);
		String body = "<h1>임시 패스워드 : " + tempPassword + "</h1>";
		body += "<a href=\"" + siteMainUri + "/usr/member/login\" target=\"_blank\">로그인 하러가기</a>";

		ResultData sendResultData = mailService.send(actor.getEmail(), title, body);

		if (sendResultData.isFail()) {
			return sendResultData;
		}

		setTempPassword(actor, tempPassword);

		return ResultData.from("S-1", "계정의 이메일주소로 임시 패스워드가 발송되었습니다.");
	}

	private void setTempPassword(Member actor, String tempPassword) {
		memberRepository.modify(actor.getId(), Ut.sha256(tempPassword), null, null, null, null);
	}

	public ResultData<Integer> join(String loginId, String loginPw, String name, String nickname, String cellphone,
									String email, String address, String authName, int authLevel) {

		// 아이디 중복 체크
		Member existsMember = getMemberByLoginId(loginId);
		if (existsMember != null) {
			return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다", loginId));
		}

		// 이름과 이메일 중복 체크
		existsMember = getMemberByNameAndEmail(name, email);
		if (existsMember != null) {
			return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 이메일(%s)입니다", name, email));
		}

		// 회원가입 처리 (필수 컬럼을 테이블에 맞게 추가)
		memberRepository.doJoin(loginId, loginPw, name, nickname, cellphone, email, address, authName, authLevel);

		// 최근 삽입된 회원 ID 조회
		int id = memberRepository.getLastInsertId();

		// 성공적으로 회원가입된 후 반환
		return ResultData.from("S-1", "회원가입 성공", "가입 성공 id", id);
	}

	public Member getMemberByNameAndEmail(String name, String email) {
		
		return memberRepository.getMemberByNameAndEmail(name, email);

	}

	public Member getMemberByLoginId(String loginId) {
		
		return memberRepository.getMemberByLoginId(loginId);
	}

	public Member getMemberById(int id) {
		return memberRepository.getMemberById(id);
	}

	public ResultData modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphone,
			String email) {

		loginPw = Ut.sha256(loginPw);

		memberRepository.modify(loginedMemberId, loginPw, name, nickname, cellphone, email);

		return ResultData.from("S-1", "회원정보 수정 완료");
	}

	public ResultData modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphone,
			String email) {
		memberRepository.modifyWithoutPw(loginedMemberId, name, nickname, cellphone, email);

		return ResultData.from("S-1", "회원정보 수정 완료");
	}

	public ResultData withdrawMember(int id) {
		memberRepository.withdraw(id);
		return ResultData.from("S-1", "탈퇴 처리 완료");
	}



	public void updateAuthLevel(int memberId, int authLevel) {
		memberRepository.updateAuthLevel(memberId, authLevel);
	}

	public List<Member> getForPrintMembers(String searchType, String searchKeyword) {
		return memberRepository.getForPrintMembersWithCert(searchType, searchKeyword);
	}


}