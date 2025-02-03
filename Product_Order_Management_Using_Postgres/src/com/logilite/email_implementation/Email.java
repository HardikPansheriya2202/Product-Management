package com.logilite.email_implementation;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;  

public class Email
{
	 public static void send(String to,String sub,String msg){
		 Properties props = new Properties();
		 props.put("mail.smtp.host", "smtp.gmail.com");
		 props.put("mail.smtp.socketFactory.port", "465");
		 props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		 props.put("mail.smtp.auth", "true");
		 props.put("mail.smtp.port", "465");
		 
		 Session session = Session.getDefaultInstance(props,
				 new javax.mail.Authenticator() {
			 protected PasswordAuthentication getPasswordAuthentication() {
				 return new PasswordAuthentication("Your email address", "Your app password");
			 }
		 });
		 
		 try
		{
			MimeMessage message = new MimeMessage(session);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(sub);
			
			message.setContent(msg, "text/html; charset=UTF-8");
						
			Transport.send(message);
			System.out.println("Email sent successfully");
		}
		catch (MessagingException e)
		{
			throw new RuntimeException(e);
		}
	 }
}
