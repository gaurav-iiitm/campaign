package com.madmax.campaign.controllers;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.madmax.campaign.models.Campaign;
import com.madmax.campaign.models.Connection;
import com.madmax.campaign.models.User;
import com.madmax.campaign.repository.CampaignRepository;
import com.madmax.campaign.repository.ConnectionRepository;
import com.madmax.campaign.repository.UserRepository;
import com.madmax.campaign.utils.Message;

@Controller
@RequestMapping("/user/connection")
public class ConnectionController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ConnectionRepository connectionRepository;
	
	@Autowired
	private CampaignRepository campaignRepository;
	
	//Adding common data to response
	@ModelAttribute
	private void addCommonData(Model model,Principal principal)
	{
		String username=principal.getName();
		User user=this.userRepository.getUserByUsername(username);
		model.addAttribute("user", user);
	}
	
	
	//Handler for showing a particular connection
	@GetMapping("/{connectionid}")
	public String showConnectionDetail(@PathVariable("connectionid")long connectionid,Model model, Principal principal)
	{
		System.out.println("Connectionid: "+connectionid);
		Optional<Connection> optionalConnection=this.connectionRepository.findById(connectionid);
		Connection connection=optionalConnection.get();
		
		//Checking whether the valid user is viewing this
		String username=principal.getName();
		User user=this.userRepository.getUserByUsername(username);
		
		if (user.getUserid()==connection.getUser().getUserid())
		{
			model.addAttribute("connection", connection);
			model.addAttribute("title", connection.getFullname());
		}
		return "normal/connection_home";
	}
	
	//Delete Connection Handler
	@PostMapping("/{connectionid}/delete")
	public String deleteConnection(@PathVariable("connectionid")long connectionid,Principal principal,HttpSession session)
	{
		try {
			User user=this.userRepository.getUserByUsername(principal.getName());
			Connection connection=this.connectionRepository.findById(connectionid).get();
			Set<Campaign> campaigns=connection.getCampaigns();
			for (Campaign campaign : campaigns)
			{
				campaign.getConnections().remove(connection);
				this.campaignRepository.save(campaign);
			}
			connection.getCampaigns().removeAll(campaigns);

			user.getConnections().remove(connection);
			this.connectionRepository.save(connection);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Connection Deleted Successfully","alert-success"));
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
		}
		return "redirect:/user/show-connections/0";
	}
	
	//open update connection form
	@PostMapping("/{connectionid}/update")
	public String openUpdateConnection(@PathVariable("connectionid")long connectionid,Model model )
	{
		model.addAttribute("title", "Update Connection");
		Connection connection=this.connectionRepository.findById(connectionid).get();
		model.addAttribute("connection", connection);
		return "normal/update_connection_form";
	}
	
	//Connection update handler
	@PostMapping("/process-update")
	public String updateConnection(@Valid @ModelAttribute Connection connection,BindingResult bindingResult,Model model,Principal principal,HttpSession session)
	{
		Connection oldConnection=this.connectionRepository.findById(connection.getConnectionid()).get();
		System.out.println("Old Connection ID: "+oldConnection.getConnectionid());
		try {
			
			if (bindingResult.hasErrors())
			{
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("connection", connection);
				return "normal/update_connection_form";
			}
			
			User user=this.userRepository.getUserByUsername(principal.getName());
			connection.setUser(user);
			connection.setCampaigns(oldConnection.getCampaigns());
			this.connectionRepository.save(connection);
			
		} catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			model.addAttribute("connection", connection);
			session.setAttribute("message", new Message("Something Went Wrong!! "+e.getMessage(),"alert-danger"));
			return "normal/update_connection_form";
		}
		
		System.out.println("Campaign title: "+connection.getFullname());
		System.out.println("Campaign id: "+connection.getConnectionid());
		session.setAttribute("message", new Message("Your connection have been updated!!","alert-success"));
		return "redirect:/user/connection/"+connection.getConnectionid();
	}
	
}
