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

    public void sendInvitationEmail(String toEmail, String firstName, String resetLink) {
        try {
            Resend resend = new Resend(apiKey);

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #003366; padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0;">AEME Energy Manager</h1>
                        <p style="color: #a0c4ff; margin: 5px 0 0 0;">Agence pour l'Économie et la Maîtrise de l'Énergie</p>
                    </div>
                    <div style="padding: 40px 30px; background-color: #f9f9f9;">
                        <h2 style="color: #003366;">Bienvenue, %s !</h2>
                        <p style="color: #555; line-height: 1.6;">
                            Votre compte sur la plateforme AEME Energy Manager a été créé.
                            Cliquez sur le bouton ci-dessous pour définir votre mot de passe
                            et accéder à votre espace.
                        </p>
                        <div style="text-align: center; margin: 40px 0;">
                            <a href="%s"
                               style="background-color: #003366; color: white; padding: 15px 35px;
                                      text-decoration: none; border-radius: 8px; font-size: 16px;
                                      font-weight: bold; display: inline-block;">
                                Activer mon compte
                            </a>
                        </div>
                        <p style="color: #888; font-size: 13px;">
                            Ce lien est valable 24 heures. Si vous n'avez pas demandé ce compte,
                            ignorez cet email.
                        </p>
                    </div>
                    <div style="background-color: #003366; padding: 20px; text-align: center;">
                        <p style="color: #a0c4ff; margin: 0; font-size: 12px;">
                            © 2026 AEME — Ministère de l'Énergie du Sénégal
                        </p>
                    </div>
                </div>
                """.formatted(firstName, resetLink);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Activation de votre compte AEME Energy Manager")
                .html(html)
                .build();

            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("Email envoyé avec succès : " + response.getId());

        } catch (ResendException e) {
            System.err.println("Erreur envoi email Resend : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'invitation");
        }
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String resetLink) {
        try {
            Resend resend = new Resend(apiKey);

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #003366; padding: 30px; text-align: center;">
                        <h1 style="color: white; margin: 0;">AEME Energy Manager</h1>
                        <p style="color: #a0c4ff; margin: 5px 0 0 0;">Agence pour l'Économie et la Maîtrise de l'Énergie</p>
                    </div>
                    <div style="padding: 40px 30px; background-color: #f9f9f9;">
                        <h2 style="color: #003366;">Réinitialisation de mot de passe</h2>
                        <p style="color: #555; line-height: 1.6;">
                            Bonjour %s, vous avez demandé une réinitialisation de votre mot de passe.
                            Cliquez sur le bouton ci-dessous pour en définir un nouveau.
                        </p>
                        <div style="text-align: center; margin: 40px 0;">
                            <a href="%s"
                               style="background-color: #003366; color: white; padding: 15px 35px;
                                      text-decoration: none; border-radius: 8px; font-size: 16px;
                                      font-weight: bold; display: inline-block;">
                                Réinitialiser mon mot de passe
                            </a>
                        </div>
                        <p style="color: #888; font-size: 13px;">
                            Ce lien est valable 24 heures. Si vous n'êtes pas à l'origine de
                            cette demande, ignorez cet email.
                        </p>
                    </div>
                    <div style="background-color: #003366; padding: 20px; text-align: center;">
                        <p style="color: #a0c4ff; margin: 0; font-size: 12px;">
                            © 2026 AEME — Ministère de l'Énergie du Sénégal
                        </p>
                    </div>
                </div>
                """.formatted(firstName, resetLink);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Réinitialisation de votre mot de passe AEME")
                .html(html)
                .build();

            resend.emails().send(params);

        } catch (ResendException e) {
            System.err.println("Erreur envoi email Resend : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }
}