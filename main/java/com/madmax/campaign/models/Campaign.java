package com.madmax.campaign.models;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Campaign {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long campaignid;
	
	@NotBlank(message="Campaign Title is required!!")
	private String title;
	
	private String description;
	
	private String phasedescription;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startdate;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate deadline;
	
	private String status;
	
	private Boolean isactive;
	
	private String image;
	@ManyToOne
	private User user;
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name="campaign_connection",
				joinColumns = @JoinColumn(name="campaignid"),
				inverseJoinColumns = @JoinColumn(name="connectionid"))
	private Set<Connection> connections=new HashSet<Connection>();
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.campaignid==((Campaign)obj).getCampaignid();
	}
	
	
}
