package com.RecetasFinal.Services;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ServicioMail {

	@Autowired
	JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String email;
	
	public void sendListEmail(String emailTo,String mensaje) {
		MimeMessage message = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message,true);
			helper.setFrom(email);
			helper.setTo(emailTo);
			helper.setSubject("Aplicacion de Recetas");
			helper.setText(mensaje);
			javaMailSender.send(message);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
