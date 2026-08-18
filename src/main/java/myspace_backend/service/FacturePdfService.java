package myspace_backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import myspace_backend.entity.Facture;
import myspace_backend.entity.Client;
import myspace_backend.exception.AccesRefuseException;
import myspace_backend.repository.FactureRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FacturePdfService {

    private final FactureRepository factureRepository;

    @Transactional(readOnly = true)
    public ByteArrayInputStream genererRecuPdf(String email, Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new AccesRefuseException("Facture introuvable"));

        // Vérifier que la facture appartient bien au client connecté
        Client client = facture.getClient();
        if (client == null || !client.getEmail().equals(email)) {
            throw new AccesRefuseException("Accès refusé à cette facture");
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Logo Al Baraka
            try {
                ClassPathResource imgResource = new ClassPathResource("static/logo-albaraka-official-transparent.png");
                if (!imgResource.exists()) {
                    imgResource = new ClassPathResource("logo-albaraka-official-transparent.png");
                }
                try (InputStream is = imgResource.getInputStream()) {
                    Image logo = Image.getInstance(is.readAllBytes());
                    logo.scaleToFit(160, 80);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                }
            } catch (Exception ignored) {}

            // Titre du reçu
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Paragraph title = new Paragraph("REÇU DE PAIEMENT DE FACTURE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15);
            title.setSpacingAfter(20);
            document.add(title);

            // Infos client & facture
            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 10);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            document.add(new Paragraph("Client : " + client.getNom() + " " + client.getPrenom(), fontInfo));
            document.add(new Paragraph("Organisme : " + facture.getOrganisme(), fontInfo));
            document.add(new Paragraph("Référence Facture : " + facture.getReferenceFacture(), fontInfo));
            document.add(new Paragraph("Montant Réglé : " + facture.getMontant() + " TND", fontInfo));

            if (facture.getDatePaiement() != null) {
                document.add(new Paragraph("Date de Transaction : " + facture.getDatePaiement().format(dtf), fontInfo));
            }
            if (facture.getRecuReference() != null) {
                document.add(new Paragraph("Référence Reçu : " + facture.getRecuReference(), fontInfo));
            }

            document.add(Chunk.NEWLINE);

            // Note de sécurité
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
            Paragraph footer = new Paragraph("Ce document est un reçu électronique officiel émis par Al Baraka Bank. Aucune signature manuscrite n'est requise.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40);
            document.add(footer);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Erreur lors de la génération du PDF du reçu", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}