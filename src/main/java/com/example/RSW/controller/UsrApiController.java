package com.example.RSW.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.service.ApiService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/usr/api")
public class UsrApiController {

	private final ApiService apiService;
	
	@Autowired
	public UsrApiController(ApiService apiService) {
		this.apiService = apiService;
	}

	/**
	 * [GET] /usr/api/dongList?district=서구 특정 구(district)에 해당하는 동(dong) 리스트 반환
	 * (JSON)
	 */
	@GetMapping("/dongList")
	@ResponseBody
	public List<String> getDongList(@RequestParam String district) {
		return apiService.getDongListByDistrict(district);
	}

	// 시(city) 리스트 반환
	@GetMapping("/cityList")
	@ResponseBody
	public List<String> getCityList() {
		return apiService.getCityList();
	}

	// 특정 시(city)에 해당하는 구(district) 리스트 반환
	@GetMapping("/districtList")
	@ResponseBody
	public List<String> getDistrictList(@RequestParam String city) {
		return apiService.getDistrictListByCity(city);
	}
	


}