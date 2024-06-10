package com.madmax.campaign.models;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.madmax.campaign.email.validEmail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Connection {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY )
	private long connectionid;
	
	@NotBlank(message="Name is required!!")
	@Size(min=3,max=20,message="Minimum 3 and Maximum 20 characters are required!!")
	private String fullname;
	
	@NotBlank(message="Email is required!!")
	@validEmail
	private String email;
	
	@NotBlank(message="Phone Number is required!!")
	@Pattern(regexp = "^[0-9]{10,10}", message = "Numbers only !!")
	private String phoneno;
	

	private String degree;
	
	@NotBlank(message="Address is required!!")
	private String address;
	
	@NotBlank(message="Work is required!!")
	private String work;
	
	@ManyToOne
	private User user;
	
	@ManyToMany(mappedBy = "connections", fetch = FetchType.LAZY)
	private Set<Campaign> campaigns=new HashSet<Campaign>();

	@Override
	public boolean equals(Object obj) {
		return this.connectionid==((Connection)obj).getConnectionid();
	}
	
	
}
