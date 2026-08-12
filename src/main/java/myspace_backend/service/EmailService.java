package myspace_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Remplace le constructeur explicite
public class EmailService {

    private final JavaMailSender mailSender;

    public void envoyerEmailOtp(String destinataire, String codeOtp) {
        String sujet = "Al Baraka - Votre code de vérification";
        String contenuHtml = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #b81d24;">Banque Al Baraka</h2>
                <p>Bonjour,</p>
                <p>Voici votre code de vérification à usage unique (2FA) :</p>
                <div style="background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #b81d24; border-radius: 5px;">
                    %s
                </div>
                <p style="margin-top: 15px; font-size: 12px; color: #777;">Ce code est valide pendant <strong>5 minutes</strong>. Ne le partagez avec personne.</p>
            </div>
            """.formatted(codeOtp);

        envoyerHtml(destinataire, sujet, contenuHtml);
    }

    /**
     * 👈 Mise à jour : Inclus l'identifiant et le mot de passe temporaire
     */
    public void envoyerEmailBienvenue(String destinataire, String identifiant, String motDePasseTemp) {
        String sujet = "Bienvenue chez Al Baraka - Activation de votre compte";
        String contenuHtml = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #b81d24;">Bienvenue chez Al Baraka</h2>
                <p>Votre espace bancaire en ligne a été créé avec succès.</p>
                <p>Voici vos identifiants pour accéder à votre espace :</p>
                
                <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0;">
                    <p style="margin: 5px 0; font-size: 15px;"><strong>Identifiant :</strong> <span style="color: #b81d24; font-weight: bold;">%s</span></p>
                    <p style="margin: 5px 0; font-size: 15px;"><strong>Mot de passe temporaire :</strong> <span style="color: #333; font-weight: bold;">%s</span></p>
                </div>
                
                <p style="margin-top: 15px; font-size: 12px; color: #777;">Lors de votre première connexion, il vous sera demandé de modifier ce mot de passe temporaire.</p>
            </div>
            """.formatted(identifiant, motDePasseTemp);

        envoyerHtml(destinataire, sujet, contenuHtml);
    }

    private void envoyerHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Échec de l'envoi de l'email : " + e.getMessage());
        }
    }
}