package com.madmax.campaign.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MailTemplate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long templateid;
	
	@NotBlank(message="Title is required !!")
	private String title;
	
	private String description;
	
	private String salutation;
	
	private String prebody;
	
	private String postbody;
	
	private String followup;
	
	private String extrainfo;
	
	private String thankstext;
	
	private String disclaimer;
	
	@ManyToOne
	private User user;
}
