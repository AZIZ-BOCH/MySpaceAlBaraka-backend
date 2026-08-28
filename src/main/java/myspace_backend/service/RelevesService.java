package myspace_backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import myspace_backend.dto.response.CompteResponse;
import myspace_backend.dto.response.SoldeMensuelResponse;
import myspace_backend.dto.response.TransactionResponse;
import myspace_backend.entity.Client;
import myspace_backend.entity.Compte;
import myspace_backend.entity.Transaction;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.AccesRefuseException;
import myspace_backend.exception.ClientNotFoundException;
import myspace_backend.exception.MoisInvalideException;
import myspace_backend.repository.CompteRepository;
import myspace_backend.repository.TransactionRepository;
import myspace_backend.repository.UtilisateurRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelevesService {

    private static final int NOMBRE_MOIS_AUTORISES = 6;

    private final UtilisateurRepository utilisateurRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<CompteResponse> listerComptes(String email) {
        Client client = recupererClientParEmail(email);

        return compteRepository.findByClient_Id(client.getId())
                .stream()
                .map(compte -> {
                    BigDecimal solde = transactionRepository
                            .findTopByCompte_IdOrderByDateOperationDescIdDesc(compte.getId())
                            .map(Transaction::getSolde)
                            .orElse(BigDecimal.ZERO);
                    return new CompteResponse(compte.getId(), compte.getRib(), solde);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> consulterReleve(String email, String rib, YearMonth mois) {
        Client client = recupererClientParEmail(email);
        validerMoisAutorise(mois);

        Compte compte = compteRepository.findByRibAndClient_Id(rib, client.getId())
                .orElseThrow(() -> new AccesRefuseException("Ce RIB ne vous appartient pas"));

        LocalDate premierJour = mois.atDay(1);
        LocalDate dernierJour = mois.atEndOfMonth();

        return transactionRepository
                .findByCompte_RibAndDateOperationBetween(compte.getRib(), premierJour, dernierJour)
                .stream()
                .map(t -> new TransactionResponse(
                        t.getDateOperation(),
                        t.getLibelle(),
                        t.getDebit(),
                        t.getCredit(),
                        t.getSolde()
                ))
                .toList();
    }

    // 👈 Évolution du solde sur les 6 derniers mois + aujourd'hui
    @Transactional(readOnly = true)
    public List<SoldeMensuelResponse> obtenirEvolutionSolde(String email, String rib) {
        Client client = recupererClientParEmail(email);

        Compte compte = compteRepository.findByRibAndClient_Id(rib, client.getId())
                .orElseThrow(() -> new AccesRefuseException("Ce RIB ne vous appartient pas"));

        List<SoldeMensuelResponse> evolution = new ArrayList<>();
        YearMonth moisActuel = YearMonth.now();

        for (int i = NOMBRE_MOIS_AUTORISES; i >= 1; i--) {
            YearMonth mois = moisActuel.minusMonths(i);
            LocalDate dernierJour = mois.atEndOfMonth();

            BigDecimal solde = transactionRepository
                    .findTopByCompte_IdAndDateOperationLessThanEqualOrderByDateOperationDescIdDesc(
                            compte.getId(), dernierJour)
                    .map(Transaction::getSolde)
                    .orElse(BigDecimal.ZERO);

            evolution.add(new SoldeMensuelResponse(mois.toString(), solde));
        }

        BigDecimal soldeActuel = transactionRepository
                .findTopByCompte_IdOrderByDateOperationDescIdDesc(compte.getId())
                .map(Transaction::getSolde)
                .orElse(BigDecimal.ZERO);

        evolution.add(new SoldeMensuelResponse("Aujourd'hui", soldeActuel));

        return evolution;
    }

    // 👈 NOUVEAU : Les 5 dernières transactions (aperçu rapide dashboard)
    @Transactional(readOnly = true)
    public List<TransactionResponse> obtenirDernieresTransactions(String email, String rib) {
        Client client = recupererClientParEmail(email);

        Compte compte = compteRepository.findByRibAndClient_Id(rib, client.getId())
                .orElseThrow(() -> new AccesRefuseException("Ce RIB ne vous appartient pas"));

        return transactionRepository
                .findTop5ByCompte_IdOrderByDateOperationDescIdDesc(compte.getId())
                .stream()
                .map(t -> new TransactionResponse(
                        t.getDateOperation(),
                        t.getLibelle(),
                        t.getDebit(),
                        t.getCredit(),
                        t.getSolde()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream genererPdfReleve(String email, String rib, YearMonth mois) {
        Client client = recupererClientParEmail(email);
        validerMoisAutorise(mois);

        Compte compte = compteRepository.findByRibAndClient_Id(rib, client.getId())
                .orElseThrow(() -> new AccesRefuseException("Ce RIB ne vous appartient pas"));

        List<TransactionResponse> transactions = consulterReleve(email, rib, mois);

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            try {
                ClassPathResource imgResource = new ClassPathResource("static/logo-albaraka-official-transparent.png");

                if (!imgResource.exists()) {
                    imgResource = new ClassPathResource("logo-albaraka-official-transparent.png");
                }

                try (InputStream is = imgResource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    Image logo = Image.getInstance(bytes);

                    logo.scaleToFit(180, 100);
                    logo.setAlignment(Element.ALIGN_CENTER);

                    document.add(logo);
                }
            } catch (Exception e) {
                // Fallback silencieux si l'image n'est pas chargée
            }

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Paragraph subTitle = new Paragraph("Relevé de Compte Mensuel - " + mois.toString(), subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(20);
            subTitle.setSpacingBefore(5);
            document.add(subTitle);

            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Titulaire : " + client.getNom() + " " + client.getPrenom(), fontInfo));
            document.add(new Paragraph("RIB : " + compte.getRib(), fontInfo));
            document.add(new Paragraph("Période : Du " + mois.atDay(1) + " au " + mois.atEndOfMonth(), fontInfo));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 4, 2, 2, 2});

            String[] headers = {"Date", "Libellé", "Débit (TND)", "Crédit (TND)", "Solde (TND)"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (TransactionResponse t : transactions) {
                table.addCell(new Phrase(t.getDate() != null ? t.getDate().format(dtf) : "", dataFont));
                table.addCell(new Phrase(t.getLibelle() != null ? t.getLibelle() : "", dataFont));
                table.addCell(new Phrase(t.getDebit() != null ? t.getDebit().toString() : "-", dataFont));
                table.addCell(new Phrase(t.getCredit() != null ? t.getCredit().toString() : "-", dataFont));
                table.addCell(new Phrase(t.getSolde() != null ? t.getSolde().toString() : "0.000", dataFont));
            }

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            throw new RuntimeException("Erreur lors de la génération du PDF", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Client recupererClientParEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("Utilisateur introuvable"));
        return utilisateur.getClient();
    }

    private void validerMoisAutorise(YearMonth mois) {
        YearMonth moisActuel = YearMonth.now();
        YearMonth moisDernierMoisRevolu = moisActuel.minusMonths(1);
        YearMonth moisMinimum = moisActuel.minusMonths(NOMBRE_MOIS_AUTORISES);

        if (mois.isAfter(moisDernierMoisRevolu) || mois.isBefore(moisMinimum)) {
            throw new MoisInvalideException(
                    "Seuls les 6 derniers mois révolus (hors mois en cours) sont consultables."
            );
        }
    }
}