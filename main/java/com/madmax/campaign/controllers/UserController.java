package com.madmax.campaign.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

import com.madmax.campaign.dto.PasswordDto;
import com.madmax.campaign.dto.UserDto;
import com.madmax.campaign.models.Campaign;
import com.madmax.campaign.models.Connection;
import com.madmax.campaign.models.User;
import com.madmax.campaign.repository.CampaignRepository;
import com.madmax.campaign.repository.ConnectionRepository;
import com.madmax.campaign.repository.UserRepository;
import com.madmax.campaign.utils.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ConnectionRepository connectionRepository;
	
	@Autowired
	private CampaignRepository campaignRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//method for adding common data for response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String username=principal.getName();
		//Get user by username
		User user=userRepository.getUserByUsername(username);
		System.out.println("USER "+user);
		model.addAttribute("user", user);
	}
	
	//Dashboard handler
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//Add connection handler
	@GetMapping("/add-connection")
	public String openAddConnectionForm(Model model)
	{
		model.addAttribute("title", "Add Connection");
		model.addAttribute("connection", new Connection());
		return "normal/add_connection_form";
	}
	
	//Processing Add Connection form
	@PostMapping("/process-connection")
	public String processConnection(@Valid @ModelAttribute Connection connection, BindingResult bindingResult ,Principal principal,Model model,HttpSession session)
	{
		try {
			
			if (bindingResult.hasErrors())
			{
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("connection", connection);
				return "normal/add_connection_form";
			}
			
			String username=principal.getName();
			User user=this.userRepository.getUserByUsername(username);
			connection.setUser(user);
			user.getConnections().add(connection);
			this.userRepository.save(user);
			System.out.println("DATA "+connection);
			model.addAttribute("connection", connection);
			session.setAttribute("message", new Message("Connection added Successfully!! Add more?","alert-success"));
			return "normal/add_connection_form";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("connection", connection);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/add_connection_form";
		}
		
		
	}
	
	//start campaign handler
	@GetMapping("/start-campaign")
	public String openStartCampaignForm(Model model)
	{
		model.addAttribute("title", "Start Campaign");
		model.addAttribute("campaign", new Campaign());
		return "normal/start_campaign_form";
	}
	
	//processing start campaign form
	@PostMapping("/process-campaign")
	public String processCampaign(@Valid @ModelAttribute Campaign campaign, BindingResult bindingResult ,@RequestParam("profileimage") MultipartFile file, Principal principal,Model model,HttpSession session)
	{
		try {
			if (bindingResult.hasErrors())
			{
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("campaign", campaign);
				return "normal/start_campaign_form";
			}
			
			if (campaign.getStartdate().compareTo(campaign.getDeadline())>0)
			{
				System.out.println("Deadline should be a later date than startdate");
				model.addAttribute("campaign", campaign);
				session.setAttribute("message", new Message("Deadline should be a later date than startdate !!","alert-danger"));
				return "normal/start_campaign_form";
			}
			
			String username=principal.getName();
			User user=this.userRepository.getUserByUsername(username);
			
			//processing and uploading campaign poster
			if (file.isEmpty())
			{
				System.out.println("No Campaign Poster Provided");
				campaign.setImage("default.jpg");
			}
			else
			{
				campaign.setImage(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Camapign Poster uploaded");
			}
			
			campaign.setPhasedescription("Starting Phase");
			campaign.setIsactive(true);
			if (campaign.getStatus().length()==0)
			{
				campaign.setStatus("Just started");
			}
			user.getCampaigns().add(campaign);
			campaign.setUser(user);
			this.userRepository.save(user);
			
			System.out.println("DATA "+campaign);
			model.addAttribute("campaign", campaign);
			session.setAttribute("message", new Message("New Campaign Successfully started!! Head to campaign Section to view.","alert-success"));
			return "normal/start_campaign_form";
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			model.addAttribute("campaign", campaign);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/start_campaign_form";
		}
	}
	
	//show connections handler
	//per-page=3[n]
	//current-page=0[current]
	@GetMapping("/show-connections/{page}")
	public String showConnetions(@PathVariable("page")int page, Model model,Principal principal)
	{
		model.addAttribute("title", "Show Connections Page");
		String username=principal.getName();
		User user=this.userRepository.getUserByUsername(username);
		
		Pageable pageable=PageRequest.of(page, 4);
		Page<Connection> connections=this.connectionRepository.findConnectionsByUser(user.getUserid(),pageable);
		model.addAttribute("connections", connections);
		model.addAttribute("currentpage", page);
		int totalpages=connections.getTotalPages();
		if (totalpages==0)
			totalpages=1;
		model.addAttribute("totalpages", totalpages);
		return "normal/show_connections";
	}
	
	//show campaign page 
	//per-page=3[n]
	//current-page=0[current]
	@GetMapping("/show-campaigns/{page}")
	public String showCampaigns(@PathVariable("page")int page, Model model, Principal principal)
	{
		model.addAttribute("title", "Show Campaigns Page");
		String username=principal.getName();
		User user=this.userRepository.getUserByUsername(username);
		Pageable pageable=PageRequest.of(page, 3);
		Page<Campaign> campaigns=this.campaignRepository.findCampaignsByUser(user.getUserid(), pageable);
		model.addAttribute("campaigns", campaigns);
		model.addAttribute("currentpage", page);
		int totalpages=campaigns.getTotalPages();
		if (totalpages==0)
			totalpages=1;
		model.addAttribute("totalpages", totalpages);
		System.out.println("Total pages "+campaigns.getTotalPages());
		return "normal/show_campaigns";
	}
	
	//User Profile page handler
	@GetMapping("/profile")
	public String userProfile(Model model)
	{
		model.addAttribute("title", "User Profile");
		return "normal/profile";
	}
	
	//Handler for processing profileImage
	@PostMapping("/process-profileimage")
	public String processProfileImage(@RequestParam("profileimage") MultipartFile file,Principal principal,Model model,HttpSession session)
	{
		try {
			User user=this.userRepository.getUserByUsername(principal.getName());
			
			if (!file.isEmpty()&&user.getImageurl()!="default.png")
			{
				//Delete old Profile image
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, user.getImageurl());
				file1.delete();
				
				//Update new poster
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				user.setImageurl(file.getOriginalFilename());
				
				this.userRepository.save(user);

			}
			if (!file.isEmpty())
				session.setAttribute("message", new Message("Profile Image Changed Successfully !!","alert-success"));
			
		} catch (Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/profile";
		}
		return "redirect:/user/settings";
	}
	
	//Handler to Show settings page
	@GetMapping("/settings")
	public String showSettings(Model model)
	{
		model.addAttribute("title", "Settings-User");
		model.addAttribute("userDto", new UserDto());
		model.addAttribute("passwordDto", new PasswordDto());
		return "normal/settings";
	}
	
	
	//Handler to edit personal info
	@PostMapping("/update-personalInfo")
	public String updatePersonalInfo(@Valid @ModelAttribute UserDto userDto, BindingResult bindingResult, Principal principal, Model model, HttpSession session)
	{
		try {
			
			if (bindingResult.hasErrors())
			{
				model.addAttribute("userDto", userDto);
				return "redirect:/user/settings";
			}
			
			User user=this.userRepository.getUserByUsername(principal.getName());
			user.setFullname(userDto.getFullname());
			user.setPhoneno(userDto.getPhoneno());
			user.setEmail(userDto.getEmail());
			user.setAddress(userDto.getAddress());
			user.setExperience(userDto.getExperience());
			user.setAbout(userDto.getAbout());
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Personal information Updated", "alert-success"));
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
		}
		return "redirect:/user/settings";
	}
	
	//handler to change password
	@PostMapping("/change-password")
	public String changePassword(@Valid @ModelAttribute PasswordDto passwordDto, BindingResult bindingResult,  Principal principal, HttpSession session)
	{
		try {
			
			if (bindingResult.hasErrors())
			{
				System.out.println("Error: "+bindingResult.toString());
				return "redirect:/user/settings";
			}
			
			User user=this.userRepository.getUserByUsername(principal.getName());
			if (!passwordEncoder.matches(passwordDto.getOriginalPassword(), user.getPassword()))
			{
				System.out.println(user.getPassword()+"  "+passwordEncoder.encode(passwordDto.getOriginalPassword()));
				session.setAttribute("message", new Message("Incorrect Original password !!","alert-danger"));
				return "redirect:/user/settings";
			}
			
			if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword()))
			{
				session.setAttribute("message", new Message("Passwords do not match !!","alert-danger"));
				return "redirect:/user/settings";
			}
			
			user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Password changed Successfully !!","alert-success"));;
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(),"alert-danger"));
		}
		
		return "redirect:/user/settings";
	}
	
	
 }
