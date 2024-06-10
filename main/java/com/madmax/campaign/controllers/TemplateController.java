package com.madmax.campaign.controllers;

import java.security.Principal;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.madmax.campaign.models.MailTemplate;
import com.madmax.campaign.models.User;
import com.madmax.campaign.repository.TemplateRepository;
import com.madmax.campaign.repository.UserRepository;
import com.madmax.campaign.utils.Message;

@Controller
@RequestMapping("/user")
public class TemplateController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TemplateRepository templateRepository;
	
	@ModelAttribute
	private void addCommonData(Model model,Principal principal)
	{
		User user=this.userRepository.getUserByUsername(principal.getName());
		model.addAttribute("user", user);
	}
	
	//Handler for opening add template form
	@GetMapping("/add-template")
	public String openAddTemplateForm(Model model) 
	{
		model.addAttribute("title", "Add template Page");
		model.addAttribute("template",new MailTemplate());
		return "normal/add_template_form";
	}
	
	//Handler for Adding new template
	@PostMapping("/process-template")
	public String addTemplate(@Valid @ModelAttribute MailTemplate template,BindingResult bindingResult,Principal principal,Model model,HttpSession session)
	{
		try {
			
			if (bindingResult.hasErrors())
			{
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("template", template);
				return "normal/add_template_form";
			}
			
			User user=this.userRepository.getUserByUsername(principal.getName());
			template.setUser(user);
			user.getTemplates().add(template);
			this.userRepository.save(user);
			model.addAttribute("template", template);
			session.setAttribute("message", new Message("Template added Successfully!! Add more?","alert-success"));
			return "normal/add_template_form";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("template", template);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/add_template_form";
		}
	}
	
	// Handler to show templates
	//per-page=3[n]
	//current-page=0[current]
	@GetMapping("/show-templates/{page}")
	public String showTemplates(@PathVariable("page")int page,Model model,Principal principal)
	{
		model.addAttribute("title", "Show Templates Page");
		User user=this.userRepository.getUserByUsername(principal.getName());
		
		Pageable pageable=PageRequest.of(page, 3);
		Page<MailTemplate> templates=this.templateRepository.findTemplatesByUser(user.getUserid(), pageable);
		model.addAttribute("templates", templates);
		model.addAttribute("currentpage", page);
		int totalpages=templates.getTotalPages();
		if (totalpages==0)
			totalpages=1;
		model.addAttribute("totalpages", totalpages);
		return "normal/show_templates";
	}
	
	//Handler to show particular template
	@GetMapping("/template/{templateid}")
	public String viewParticularTemplate(@PathVariable("templateid") long templateid,Model model)
	{
		model.addAttribute("title", "Template View Page");
		MailTemplate template=this.templateRepository.findById(templateid).get();
		model.addAttribute("template", template);
		return "normal/show_template";
	}
	
	//handler to delete template
	@PostMapping("/template/{templateid}/delete")
	public String deleteTemplate(@PathVariable("templateid")long templateid,Principal principal,HttpSession session)
	{
		try {
			User user=this.userRepository.getUserByUsername(principal.getName());
			MailTemplate template=this.templateRepository.findById(templateid).get();
			user.getTemplates().remove(template);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Template Deleted Successfully","alert-success"));
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
		}
		return "redirect:/user/show-templates/0";
	}
	
	//handler to open update template form
	@PostMapping("/template/{templateid}/update")
	public String openUpdateTemplateForm(@PathVariable("templateid")long templateid, Model model)
	{
		model.addAttribute("title", "Update Template Page");
		MailTemplate template=this.templateRepository.findById(templateid).get();
		model.addAttribute("template", template);
		return "normal/update_template_form";
	}
	
	//handler to update template
	@PostMapping("/template/process-update")
	public String updateTemplate(@Valid @ModelAttribute MailTemplate template, BindingResult bindingResult, Model model, Principal principal,HttpSession session)
	{
		MailTemplate oldTemplate=this.templateRepository.findById(template.getTemplateid()).get();
		System.out.println("Old Template ID: "+oldTemplate.getTemplateid());
		
		try {
			
			if (bindingResult.hasErrors())
			{
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("template", template);
				return "normal/update_connection_form";
			}
			
			User user=this.userRepository.getUserByUsername(principal.getName());
			template.setUser(user);
			this.templateRepository.save(template);
			
		} catch (Exception e) {
			
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			model.addAttribute("template", template);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/update_connection_form";
		}
		System.out.println("Template title: "+template.getTitle());
		System.out.println("Template ID: "+template.getTemplateid());
		session.setAttribute("message", new Message("Your Template has been updated!!","alert-success"));
		return "redirect:/user/template/"+template.getTemplateid();
	}
	
}
