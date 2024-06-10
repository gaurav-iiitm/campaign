package com.madmax.campaign.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.madmax.campaign.email.validEmail;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long userid;
	
	@NotBlank(message="Username is required!!")
	@Size(min=2,max=16,message = "Minimum 2 and Maximum 20 characters are allowed!!")
	@Column(unique = true)
	private String username;
	
	@NotBlank(message="Password is required!!")
	private String password;
	
	@NotBlank(message="Name is required!!")
	@Size(min=2,max=20,message = "Minimum 3 and Maximum 20 characters are allowed!!")
	private String fullname;
	
	@NotBlank(message="Email is required!!")
	@validEmail
	private String email;
	
	@NotBlank(message="Phone Number is required!!")
	@Pattern(regexp = "^[0-9]{10,10}", message = "Numbers only !!")
	private String phoneno;
	
	@NotBlank(message="Experience is required!!")
	private String experience;
	
	@NotBlank(message="Address is required!!")
	private String address;
	
	private String about;
	private String role;
	private String imageurl;
	private Boolean isactive;
	@OneToMany(targetEntity = Campaign.class, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private Set<Campaign> campaigns=new HashSet<Campaign>();
	@OneToMany(targetEntity = Connection.class, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private Set<Connection> connections=new HashSet<Connection>();
	
	@OneToMany(targetEntity = MailTemplate.class, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private Set<MailTemplate> templates=new HashSet<MailTemplate>();
}
