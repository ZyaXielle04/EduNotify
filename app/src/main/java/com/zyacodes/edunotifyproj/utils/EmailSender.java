package com.zyacodes.edunotifyproj.utils;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.os.AsyncTask;
import android.util.Log;

public class EmailSender {

    private static final String TAG = "EmailSender";

    // Replace these with your own
    private static final String SENDER_EMAIL = "zyacodesservices@gmail.com";
    private static final String APP_PASSWORD = "ntia xfsk yzni xqwj";

    public static void sendEmail(String recipient, String subject, String body) {
        AsyncTask.execute(() -> {
            try {
                Log.d(TAG, "Preparing email properties...");

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                            }
                        });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setText(body);

                Log.d(TAG, "Sending email...");
                Transport.send(message);
                Log.d(TAG, "Email sent successfully!");
            } catch (MessagingException e) {
                Log.e(TAG, "Error sending email: ", e);
            }
        });
    }
}
