package com.madmax.campaign.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AboutController {
	@RequestMapping("/about")
	public String home(Model model)
	{
		model.addAttribute("title","About");
		return "about";
	}
}
