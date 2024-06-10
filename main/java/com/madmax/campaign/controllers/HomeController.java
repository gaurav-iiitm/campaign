package com.madmax.campaign.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
public class HomeController {

	
	@RequestMapping("/")
	public String home(Model model,Principal principal)
	{
		model.addAttribute("title","Home-Campaign Management System");
		return "home.html";
	}
}
