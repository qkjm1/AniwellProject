package com.example.RSW;


import com.example.RSW.interceptor.BeforeActionInterceptor;
import com.example.RSW.interceptor.NeedLoginInterceptor;
import com.example.RSW.interceptor.NeedLogoutInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;


@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    // BeforeActionInterceptor 불러오기(연결)
    @Autowired
    BeforeActionInterceptor beforeActionInterceptor;

    // NeedLoginInterceptor 불러오기(연결)
    @Autowired
    NeedLoginInterceptor needLoginInterceptor;

    // NeedLogoutInterceptor 불러오기(연결)
    @Autowired
    NeedLogoutInterceptor needLogoutInterceptor;



    // 인터셉터 등록(적용)
    public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(beforeActionInterceptor).addPathPatterns("/**").excludePathPatterns("/resource/**")
//				.excludePathPatterns("/error");
//
//		registry.addInterceptor(needLoginInterceptor).addPathPatterns("/usr/article/write")
//				.addPathPatterns("/usr/article/doWrite").addPathPatterns("/usr/article/modify")
//				.addPathPatterns("/usr/article/doModify").addPathPatterns("/usr/article/doDelete")
//				.addPathPatterns("/usr/member/doLogout");
//
//		registry.addInterceptor(needLogoutInterceptor).addPathPatterns("/usr/member/login")
//				.addPathPatterns("/usr/member/doLogin").addPathPatterns("/usr/member/join")
//				.addPathPatterns("/usr/member/doJoin");

        InterceptorRegistration ir;

        ir = registry.addInterceptor(beforeActionInterceptor);
        ir.addPathPatterns("/**");
        ir.addPathPatterns("/favicon.ico");
        ir.excludePathPatterns("/resource/**");
        ir.excludePathPatterns("/error");

//		로그인 필요
        ir = registry.addInterceptor(needLoginInterceptor);
//		글 관련
        ir.addPathPatterns("/usr/article/write");
        ir.addPathPatterns("/usr/article/doWrite");
        ir.addPathPatterns("/usr/article/modify");
        ir.addPathPatterns("/usr/article/doModify");
        ir.addPathPatterns("/usr/article/doDelete");

//		회원관련
        ir.addPathPatterns("/usr/member/myPage");
        ir.addPathPatterns("/usr/member/checkPw");
        ir.addPathPatterns("/usr/member/doCheckPw");
        ir.addPathPatterns("/usr/member/doLogout");
        ir.addPathPatterns("/usr/member/modify");
        ir.addPathPatterns("/usr/member/doModify");

//		댓글 관련
        ir.addPathPatterns("/usr/reply/doWrite");

//		좋아요 싫어요
        ir.addPathPatterns("/usr/reactionPoint/doGoodReaction");
        ir.addPathPatterns("/usr/reactionPoint/doBadReaction");

//		로그아웃 필요
        ir = registry.addInterceptor(needLogoutInterceptor);
        ir.addPathPatterns("/usr/member/login");
        ir.addPathPatterns("/usr/member/doLogin");
        ir.addPathPatterns("/usr/member/join");
        ir.addPathPatterns("/usr/member/doJoin");
        ir.addPathPatterns("/usr/member/findLoginId");
        ir.addPathPatterns("/usr/member/doFindLoginId");
        ir.addPathPatterns("/usr/member/findLoginPw");
        ir.addPathPatterns("/usr/member/doFindLoginPw");


    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")  // 웹에서 요청할 경로
                .addResourceLocations("file:/Users/e-suul/Desktop/aniwell_uploads/"); // 실제 로컬 폴더
    }
    }

