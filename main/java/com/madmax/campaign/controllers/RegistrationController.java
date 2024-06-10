package com.madmax.campaign.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.madmax.campaign.models.User;
import com.madmax.campaign.repository.UserRepository;
import com.madmax.campaign.utils.Message;

@Controller
public class RegistrationController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title", "Signup-Campaign Management System");
		model.addAttribute("user", new User());
		return "signup";
	}
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingresult, @RequestParam(value="agreement",defaultValue = "false") boolean agreement, Model model , HttpSession session)
	{
		
		try {
			
			if (!agreement)
			{
				System.out.println("You have not accepted terms and conditions");
				throw new Exception("You have not accepted terms and conditions");
			}
			
			if (bindingresult.hasErrors())
			{
				System.out.println("ERROR "+bindingresult.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			if (this.userRepository.getUserByUsername(user.getUsername())!=null)
			{
				System.out.println("Username is already taken !!");
				model.addAttribute("user", user);
				session.setAttribute("message", new Message("Username is already taken !!","alert-danger"));
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setIsactive(true);
			user.setImageurl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("USER "+user);
			
			User result=this.userRepository.save(user);
			
			model.addAttribute("user", result);
			session.setAttribute("message", new Message("Succesfully registered!!","alert-success"));
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
}
