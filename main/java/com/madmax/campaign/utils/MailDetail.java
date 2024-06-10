package com.madmax.campaign.utils;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MailDetail {

	private String subject;
	private String mainbody;
	private String attachment;
}
