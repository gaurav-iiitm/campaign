package com.madmax.campaign.services;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService {

	private final TemplateEngine templateEngine;
	private final JavaMailSenderImpl javaMailSenderImpl;
	
	public String sendmail(String toemail, String subject, Map<String, Object> variables, String file, String campaignPoster) throws MessagingException, IOException
	{
		Context context=new Context();
		for (Entry<String, Object> variable : variables.entrySet()) {
	        context.setVariable(variable.getKey(), variable.getValue());
	    }
		String process=templateEngine.process("normal/mailtemplate/template", context);
		javax.mail.internet.MimeMessage mimeMessage=javaMailSenderImpl.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage, true);
		FileSystemResource posterAttachment=new FileSystemResource("C:\\Users\\MADMAX\\Documents\\workspace-spring\\campaign\\target\\classes\\static\\img\\"+campaignPoster);
		helper.addAttachment(posterAttachment.getFilename(), posterAttachment);
		helper.setSubject(subject);
		helper.setText(process,true);
		helper.setTo(toemail);
		
		if (!file.equals(""))
		{
			FileSystemResource attachment=new FileSystemResource("C:\\Users\\MADMAX\\Documents\\workspace-spring\\campaign\\target\\classes\\static\\img\\"+file);
			helper.addAttachment(attachment.getFilename(), attachment);
		}
		
		javaMailSenderImpl.send(mimeMessage);
		return "sent";
	}
}
