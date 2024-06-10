package com.madmax.campaign.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PasswordDto {
	
	@NotBlank
	private String originalPassword;
	
	@NotBlank
	private String newPassword;
	
	@NotBlank
	private String confirmNewPassword;

}
