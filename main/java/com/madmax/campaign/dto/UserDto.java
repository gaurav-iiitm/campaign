package com.madmax.campaign.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.madmax.campaign.email.validEmail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
	
	@NotBlank
	@Size(min=2,max=20,message = "Minimum 3 and Maximum 20 characters are allowed!!")
	private String fullname;
	
	@NotBlank(message="Phone Number is required!!")
	@Pattern(regexp = "^[0-9]{10,10}", message = "Numbers only !!")
	private String phoneno;
	
	@NotBlank(message="Email is required!!")
	@validEmail
	private String email;
	
	@NotBlank(message="Address is required!!")
	private String address;
	
	@NotBlank(message="Experience is required!!")
	private String experience;
	
	private String about;

}
