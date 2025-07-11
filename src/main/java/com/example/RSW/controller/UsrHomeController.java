package com.example.RSW.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UsrHomeController {

    @RequestMapping("/usr/home/main")
    public String showMain() {
        return "/usr/home/main";
    }

    @RequestMapping("/")
    public String showMain2() {
        return "redirect:/usr/home/main";
    }
    
    @RequestMapping("/hello")
    public String hello(Model model) {
    	model.addAttribute("name", "정민");
        return "petPage";
    }


}