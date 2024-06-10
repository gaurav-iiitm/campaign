package com.madmax.campaign.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.madmax.campaign.models.Campaign;
import com.madmax.campaign.models.Connection;
import com.madmax.campaign.models.MailTemplate;
import com.madmax.campaign.models.User;
import com.madmax.campaign.repository.CampaignRepository;
import com.madmax.campaign.repository.ConnectionRepository;
import com.madmax.campaign.repository.TemplateRepository;
import com.madmax.campaign.repository.UserRepository;
import com.madmax.campaign.services.EmailService;
import com.madmax.campaign.utils.MailDetail;
import com.madmax.campaign.utils.Message;

@Controller
@RequestMapping("/user/campaign")
public class CampaignController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CampaignRepository campaignRepository;
	
	@Autowired
	private ConnectionRepository connectionRepository;
	
	@Autowired
	private TemplateRepository templateRepository;
	
	@Autowired
	private EmailService emailService;
	
	//adding common data to response
	@ModelAttribute
	private void addCommonData(Model model, Principal principal)
	{
		String username=principal.getName();
		User user=userRepository.getUserByUsername(username);
		System.out.println("USER "+user);
		model.addAttribute("user", user);
	}
	
	//Handler for showing particular campaign
	@GetMapping("/{campaignid}")
	public String showCampaignDetail(@PathVariable("campaignid")long campaignid,Model model,Principal principal)
	{
		System.out.println("Campaignid "+campaignid);
		Optional<Campaign> campiagnOptional=this.campaignRepository.findById(campaignid);
		Campaign campaign=campiagnOptional.get();
		
		//Checking whether the same user is viewing this
		String username=principal.getName();
		User user=this.userRepository.getUserByUsername(username);
		
		if (user.getUserid()==campaign.getUser().getUserid())
		{
			model.addAttribute("campaign", campaign);
			model.addAttribute("title", campaign.getTitle());
		}
		return "normal/campaign_home";
	}
	
	// Open update Campaign form handler
	@PostMapping("/{campaignid}/update")
	public String openUpdateCampaign(@PathVariable("campaignid")long campaignid,Model model)
	{
		model.addAttribute("title", "Update Campaign Detail");
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		model.addAttribute("campaign", campaign);
		return "normal/update_campaign_form";
	}
	
	
	//Delete Campaign handler
	
	//Campaign Update Handler
	@PostMapping("/process-update")
	public String updateCampaign(@Valid @ModelAttribute Campaign campaign, BindingResult bindingResult ,@RequestParam("profileimage")MultipartFile file,Model model,Principal principal,HttpSession session)
	{
		//fetching old campaign detail
		Campaign oldCampaign=this.campaignRepository.findById(campaign.getCampaignid()).get();
		try {
			
			if (bindingResult.hasErrors())
			{
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("campaign", campaign);
				return "normal/update_campaign_form";
			}
			if (campaign.getStartdate().compareTo(campaign.getDeadline())>0)
			{
				System.out.println("Deadline should be a later date than startdate");
				model.addAttribute("campaign", campaign);
				session.setAttribute("message", new Message("Deadline should be a later date than startdate !!","alert-danger"));
				return "normal/update_campaign_form";
			}
			
			if (!file.isEmpty()&&oldCampaign.getImage()!="default.jpg")
			{
				//Delete old Poster
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldCampaign.getImage());
				file1.delete();
				
				//Update new poster
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				campaign.setImage(file.getOriginalFilename());
			}
			else
			{
				campaign.setImage(oldCampaign.getImage());
			}
			User user=this.userRepository.getUserByUsername(principal.getName());
			campaign.setUser(user);
			campaign.setConnections(oldCampaign.getConnections());
			this.campaignRepository.save(campaign);
			
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			model.addAttribute("campaign", campaign);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/update_campaign_form";

		}
		System.out.println("Campaign title: "+campaign.getTitle());
		System.out.println("Campaign id: "+campaign.getCampaignid());
		session.setAttribute("message", new Message("Your campaign details have been updated!!","alert-success"));
		return "redirect:/user/campaign/"+campaign.getCampaignid();
	}
	
	//Handler to end a campaign
	@PostMapping("/{campaignid}/delete")
	public String deleteCampaign(@PathVariable("campaignid")long campaignid, Model model,Principal principal, HttpSession session)
	{
		try {
			
			Campaign campaign=this.campaignRepository.findById(campaignid).get();
			User user=this.userRepository.getUserByUsername(principal.getName());
			Set<Connection> connections=campaign.getConnections();
			
			// Deleting Campaign Poster
			if (campaign.getImage()!="default.jpg")
			{
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, campaign.getImage());
				file1.delete();
			}
			
			for (Connection connection : connections)
			{
				connection.getCampaigns().remove(campaign);
				this.connectionRepository.save(connection);
			}
			
			campaign.getConnections().removeAll(connections);
			user.getCampaigns().remove(campaign);
			this.campaignRepository.save(campaign);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Campaign Ended Successfully !!","alert-success"));
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
		}
		return "redirect:/user/show-campaigns/0";
	}
	
	//Handler to show add/remove connection to/from campaign
	@GetMapping("{campaignid}/add-or-remove-connection")
	private String openAddOrRemoveConnection(@PathVariable("campaignid") long campaignid,Model model,Principal principal)
	{
		System.out.println("CampaignId: "+campaignid);
		model.addAttribute("title", "Add or Remove Campaign-Connections");
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		model.addAttribute("campaign", campaign);
		Set<Connection> campaign_connections=campaign.getConnections();
		model.addAttribute("campaign_connections",campaign_connections );
		User user=this.userRepository.getUserByUsername(principal.getName());
		Set<Connection> connections=user.getConnections();
		model.addAttribute("connections", connections);
		return "normal/add_or_remove_connection";
	}
	
	
	//Show Campaign-connections handler
	//per-page=3[n]
	//current-page=0[current]
	@GetMapping("/{campaignid}/connections/{page}")
	private String showCampaignConnections(@PathVariable("campaignid")long campaignid,@PathVariable("page")int page,Model model)
	{
		model.addAttribute("title","Campaign-Connections");
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		model.addAttribute("campaign", campaign);
		Pageable pageable=PageRequest.of(page, 3);
		Page<Connection> connections=this.connectionRepository.findConnectionByCampaign(campaignid, pageable);
		model.addAttribute("connections", connections);
		model.addAttribute("currentpage", page);
		int totalpages=connections.getTotalPages();
		if (totalpages==0)
			totalpages=1;
		model.addAttribute("totalpages", totalpages);
		return "normal/show_campaign-connections";
	}
	
	//Handler to add or remove campaign connections
	@PostMapping("/{campaignid}/process-add-or-remove-connection")
	private String addOrRemoveConnection(@PathVariable("campaignid")long campaignid, @RequestParam("idChecked") List<Long> ids,Model model,Principal principal,HttpSession session)
	{
		System.out.println("The Ids are "+ids);
		
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		Set<Connection> old_connections=campaign.getConnections();
		for (Connection old_connection : old_connections)
		{
			old_connection.getCampaigns().remove(campaign);
			this.connectionRepository.save(old_connection);
		}
		campaign.getConnections().removeAll(old_connections);
		for (int i=0;i<ids.size();i++)
		{
			Connection connection=this.connectionRepository.findById(ids.get(i)).get();
			campaign.getConnections().add(connection);
			connection.getCampaigns().add(campaign);
			this.connectionRepository.save(connection);
		}
		this.campaignRepository.save(campaign);
		System.out.println(campaign.getConnections().size());
		session.setAttribute("message", new Message("Changes made Succesfully !!","alert-success"));
		return "redirect:/user/campaign/"+campaign.getCampaignid()+"/connections/0";
	}
	
	//Handler to remove single connection from campaign
	@PostMapping("/{campaignid}/connections/{connectionid}/remove-connection")
	public String removeConnection(@PathVariable("campaignid")long campaignid,@PathVariable("connectionid")long connectionid,HttpSession session)
	{
		try {
			Campaign campaign=this.campaignRepository.findById(campaignid).get();
			Connection connection=this.connectionRepository.findById(connectionid).get();
			campaign.getConnections().remove(connection);
			connection.getCampaigns().remove(campaign);
			this.campaignRepository.save(campaign);
			this.connectionRepository.save(connection);
			session.setAttribute("message", new Message("Connection Removed from Campaign successfullly !!","alert-success"));
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
		}
		return "redirect:/user/campaign/"+campaignid+"/connections/0";
	}
	
	//Handler to view Particular Campaign-connection
	@GetMapping("/{campaignid}/connections/view/{connectionid}")
	public String showCampaignConnection(@PathVariable("campaignid")long campaignid,@PathVariable("connectionid")long connectionid,Model model)
	{
		model.addAttribute("title","Camapign-Connection Details");
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		model.addAttribute("campaign",campaign);
		Connection connection=this.connectionRepository.findById(connectionid).get();
		model.addAttribute("connection", connection);
		return "normal/campaign-connection_home";
	}
	
	//Handler to view select template page
	//per-page=3[n]
	//current-page=0[current]
	@GetMapping("/{campaignid}/send-mail/{page}")
	public String showSelectTemplatePage(@PathVariable("page")int page, @PathVariable("campaignid") long campaignid, Model model,Principal principal)
	{
		model.addAttribute("title", "Select Template");
		User user=this.userRepository.getUserByUsername(principal.getName());
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		model.addAttribute("campaign",campaign);
		Pageable pageable=PageRequest.of(page, 3);
		Page<MailTemplate> templates=this.templateRepository.findTemplatesByUser(user.getUserid(), pageable);
		model.addAttribute("templates", templates);
		model.addAttribute("currentpage", page);
		int totalpages=templates.getTotalPages();
		if (totalpages==0)
			totalpages=1;
		model.addAttribute("totalpages", totalpages);
		return "normal/select_template";
	}
	
	//handler to show chosen template
	@GetMapping("/{campaignid}/send-mail/template/view/{templateid}")
	public String showChosenTemplate(Model model, @PathVariable("campaignid")long campaignid, @PathVariable("templateid")long templateid)
	{
		model.addAttribute("title", "Template View");
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		model.addAttribute("campaign", campaign);
		MailTemplate template=this.templateRepository.findById(templateid).get();
		model.addAttribute("template", template);
		return "normal/show_chosen_template";
	}
	
	//Handler to view Page to take Mail details
	@PostMapping("/{campaignid}/send-mail/template/{templateid}/mail-detail")
	public String mailDetails(@PathVariable("campaignid")long campaignid, @PathVariable("templateid")long templateid, Model model)
	{
		MailTemplate template=this.templateRepository.findById(templateid).get();
		Campaign campaign=this.campaignRepository.findById(campaignid).get();
		MailDetail mail_detail=new MailDetail();
		model.addAttribute("title", "Mail Detail");
		model.addAttribute("campaign", campaign);
		model.addAttribute("template", template);
		model.addAttribute("mail_detail", mail_detail);
		return "normal/mail_details";
	}
	
	//Handler for Sending mail
	@PostMapping("/{campaignid}/send-mail/template/{templateid}/send")
	public String processSendEmail(@ModelAttribute MailDetail mail_detail, @RequestParam("attachmentfile") MultipartFile file, @PathVariable("campaignid")long campaignid, @PathVariable("templateid")long templateid, Principal principal, HttpSession session) throws MessagingException, IOException
	{
		try {
			
			String filename="";
			if (!file.isEmpty())
			{
				filename=file.getOriginalFilename();
				File savefile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			User user=this.userRepository.getUserByUsername(principal.getName());
			Campaign campaign=this.campaignRepository.findById(campaignid).get();
			MailTemplate template=this.templateRepository.findById(templateid).get();
			Set<Connection> connections=campaign.getConnections();
			Map<String, Object> variables=new HashMap<String,Object>();
			variables.put("template", template);
			variables.put("campaign", campaign);
			variables.put("mail_detail", mail_detail);
			variables.put("user", user);
			Date date = new Date();
		    String str = String.format("Date/Time : %tc", date );
		    variables.put("date", str);
			for (Connection connection : connections)
			{
				variables.put("connection", connection);
				emailService.sendmail(connection.getEmail(), mail_detail.getSubject(), variables, filename, campaign.getImage());
			}
			if (!file.isEmpty())
			{
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, filename);
				file1.delete();
			}
			session.setAttribute("message", new Message("Mail Sent to all connections Successfully !!","alert-success"));
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error :"+ e.getMessage());
			session.setAttribute("message", new Message("Someting went wrong !!Please ensure that the email ids are correct","alert-danger"));
		}
		return "redirect:/user/campaign/"+campaignid;
	}
	
}
