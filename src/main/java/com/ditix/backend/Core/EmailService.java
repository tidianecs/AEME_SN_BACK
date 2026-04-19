package com.ditix.backend.Core;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    public String getFromEmail() {
        return fromEmail;
    }

    public void sendInvitationEmail(
            String toEmail,
            String firstName,
            String tempPassword,
            String frontendUrl
    ) {
        try {
            Resend resend = new Resend(apiKey);

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #003366; padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0;">AEME Energy Manager</h1>
                        <p style="color: #a0c4ff; margin: 5px 0 0 0;">
                            Agence pour l'Économie et la Maîtrise de l'Énergie
                        </p>
                    </div>
                    <div style="padding: 40px 30px; background-color: #f9f9f9;">
                        <h2 style="color: #003366;">Bienvenue, %s !</h2>
                        <p style="color: #555; line-height: 1.6;">
                            Votre compte sur la plateforme AEME Energy Manager a été créé
                            par un administrateur. Voici vos identifiants pour votre
                            première connexion :
                        </p>
                        <div style="background-color: #e8f4fd; border-left: 4px solid #003366;
                                    padding: 20px; border-radius: 4px; margin: 25px 0;">
                            <p style="margin: 0 0 10px 0; color: #333;">
                                <strong>Email :</strong> %s
                            </p>
                            <p style="margin: 0; color: #333;">
                                <strong>Mot de passe temporaire :</strong>
                                <span style="font-family: monospace; background-color: #fff;
                                             padding: 3px 8px; border-radius: 3px;
                                             font-size: 16px; letter-spacing: 2px;">
                                    %s
                                </span>
                            </p>
                        </div>
                        <p style="color: #555; line-height: 1.6;">
                            Lors de votre première connexion, vous serez invité à :
                        </p>
                        <ul style="color: #555; line-height: 1.8;">
                            <li>Changer votre mot de passe</li>
                            <li>Mettre à jour votre profil</li>
                        </ul>
                        <div style="text-align: center; margin: 40px 0;">
                            <a href="%s"
                               style="background-color: #003366; color: white; padding: 15px 35px;
                                      text-decoration: none; border-radius: 8px; font-size: 16px;
                                      font-weight: bold; display: inline-block;">
                                Accéder à la plateforme
                            </a>
                        </div>
                        <p style="color: #888; font-size: 13px;">
                            Pour des raisons de sécurité, veuillez changer votre mot de passe
                            dès votre première connexion. Ne partagez pas vos identifiants.
                        </p>
                    </div>
                    <div style="background-color: #003366; padding: 20px; text-align: center;">
                        <p style="color: #a0c4ff; margin: 0; font-size: 12px;">
                            © 2026 AEME — Ministère de l'Énergie du Sénégal
                        </p>
                    </div>
                </div>
                """.formatted(firstName, toEmail, tempPassword, frontendUrl);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Votre accès à la plateforme AEME Energy Manager")
                .html(html)
                .build();

            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("Email envoyé avec succès : " + response.getId());

        } catch (ResendException e) {
            System.err.println("Erreur envoi email Resend : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'invitation");
        }
    }

    public void sendPasswordResetEmail(
            String toEmail,
            String firstName,
            String tempPassword,
            String frontendUrl
    ) {
        try {
            Resend resend = new Resend(apiKey);

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #003366; padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0;">AEME Energy Manager</h1>
                        <p style="color: #a0c4ff; margin: 5px 0 0 0;">
                            Agence pour l'Économie et la Maîtrise de l'Énergie
                        </p>
                    </div>
                    <div style="padding: 40px 30px; background-color: #f9f9f9;">
                        <h2 style="color: #003366;">Réinitialisation de mot de passe</h2>
                        <p style="color: #555; line-height: 1.6;">
                            Bonjour %s, voici votre nouveau mot de passe temporaire :
                        </p>
                        <div style="background-color: #e8f4fd; border-left: 4px solid #003366;
                                    padding: 20px; border-radius: 4px; margin: 25px 0;">
                            <p style="margin: 0 0 10px 0; color: #333;">
                                <strong>Email :</strong> %s
                            </p>
                            <p style="margin: 0; color: #333;">
                                <strong>Mot de passe temporaire :</strong>
                                <span style="font-family: monospace; background-color: #fff;
                                             padding: 3px 8px; border-radius: 3px;
                                             font-size: 16px; letter-spacing: 2px;">
                                    %s
                                </span>
                            </p>
                        </div>
                        <div style="text-align: center; margin: 40px 0;">
                            <a href="%s"
                               style="background-color: #003366; color: white; padding: 15px 35px;
                                      text-decoration: none; border-radius: 8px; font-size: 16px;
                                      font-weight: bold; display: inline-block;">
                                Se connecter
                            </a>
                        </div>
                        <p style="color: #888; font-size: 13px;">
                            Si vous n'êtes pas à l'origine de cette demande, contactez
                            votre administrateur immédiatement.
                        </p>
                    </div>
                    <div style="background-color: #003366; padding: 20px; text-align: center;">
                        <p style="color: #a0c4ff; margin: 0; font-size: 12px;">
                            © 2026 AEME — Ministère de l'Énergie du Sénégal
                        </p>
                    </div>
                </div>
                """.formatted(firstName, toEmail, tempPassword, frontendUrl);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Réinitialisation de votre mot de passe AEME")
                .html(html)
                .build();

            resend.emails().send(params);
            System.out.println("Email reset envoyé à : " + toEmail);

        } catch (ResendException e) {
            System.err.println("Erreur envoi email reset : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }
}