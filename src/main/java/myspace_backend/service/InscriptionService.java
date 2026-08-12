package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.ConfirmerInscriptionRequest;
import myspace_backend.dto.request.EnvoyerCodeRequest;
import myspace_backend.dto.request.VerifierIdentiteRequest;
import myspace_backend.dto.response.InscriptionResponse;
import myspace_backend.dto.response.VerifierIdentiteResponse;
import myspace_backend.entity.*;
import myspace_backend.exception.*;
import myspace_backend.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private static final int DUREE_VALIDITE_CODE_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CodeOtpRepository codeOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public VerifierIdentiteResponse verifierIdentite(VerifierIdentiteRequest request) {
        Compte compte = compteRepository.findByRib(request.getRib())
                .orElseThrow(() -> new ClientNotFoundException("Aucun compte trouvé pour ce RIB"));

        Client client = compte.getClient();

        boolean documentValide = switch (request.getTypeDocument()) {
            case CIN -> request.getNumeroDocument().equals(client.getCin());
            case PASSEPORT -> request.getNumeroDocument().equals(client.getPasseport());
        };

        if (!documentValide) {
            throw new ClientNotFoundException("Le document ne correspond pas au titulaire de ce RIB");
        }

        if (utilisateurRepository.existsByClient_Id(client.getId())) {
            throw new CompteDejaExistantException("Un compte existe déjà pour ce client");
        }

        return new VerifierIdentiteResponse(
                client.getId(),
                masquerEmail(client.getEmail()),
                masquerTelephone(client.getTelephone())
        );
    }

    @Transactional
    public void envoyerCodeVerification(EnvoyerCodeRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client introuvable"));

        // Invalider les anciens OTPs du client
        codeOtpRepository.invaliderAnciensOtpsClient(client.getId());

        String code = String.valueOf(100000 + RANDOM.nextInt(900000));

        // Enregistrement dans CodeOtp
        CodeOtp codeOtp = CodeOtp.builder()
                .code(code)
                .canal(request.getCanal())
                .dateExpiration(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_CODE_MINUTES))
                .utilise(false)
                .client(client)
                .build();

        codeOtpRepository.save(codeOtp);

        if (request.getCanal() == CanalEnvoi.EMAIL) {
            emailService.envoyerEmailOtp(client.getEmail(), code);
        }
    }

    @Transactional
    public InscriptionResponse confirmerInscription(ConfirmerInscriptionRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client introuvable"));

        // Recherche dans CodeOtp
        CodeOtp codeOtp = codeOtpRepository
                .findFirstByClient_IdAndCodeAndUtiliseFalseOrderByIdDesc(request.getClientId(), request.getCode())
                .orElseThrow(() -> new CodeOtpInvalideException("Code de vérification invalide"));

        if (codeOtp.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new CodeExpireException("Ce code de vérification a expiré");
        }

        codeOtp.setUtilise(true);
        codeOtpRepository.save(codeOtp);

        if (utilisateurRepository.existsByClient_Id(client.getId())) {
            throw new CompteDejaExistantException("Un compte existe déjà pour ce client");
        }

        String motDePasseTemporaire = genererMotDePasseTemporaire();

        // 👈 MODIFICATION : Génération de l'identifiant basé sur prenom.nom
        String prenomClean = client.getPrenom().trim().toLowerCase().replaceAll("\\s+", "");
        String nomClean = client.getNom().trim().toLowerCase().replaceAll("\\s+", "");

        String identifiantBase = prenomClean + "." + nomClean; // ex: aziz.bouchriha
        String identifiant = identifiantBase;

        int compteur = 1;
        while (utilisateurRepository.existsByIdentifiant(identifiant)) {
            identifiant = identifiantBase + compteur; // ex: aziz.bouchriha1 s'il existe déjà
            compteur++;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdentifiant(identifiant);
        utilisateur.setEmail(client.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        utilisateur.setPremiereConnexion(true);
        utilisateur.setClient(client);

        utilisateurRepository.save(utilisateur);

        // Envoi de l'email avec le nouvel Identifiant (prenom.nom) ET Mot de passe temporaire
        emailService.envoyerEmailBienvenue(client.getEmail(), identifiant, motDePasseTemporaire);

        return new InscriptionResponse(
                "Inscription réussie. Vos identifiants ont été envoyés par e-mail.",
                codeOtp.getCanal().name()
        );
    }

    private String genererMotDePasseTemporaire() {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(caracteres.charAt(RANDOM.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    private String masquerEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int index = email.indexOf('@');
        if (index <= 1) return "***" + email.substring(index);
        return email.substring(0, 1) + "***" + email.substring(index - 1);
    }

    private String masquerTelephone(String telephone) {
        if (telephone == null || telephone.length() < 4) return "***";
        return "***" + telephone.substring(telephone.length() - 4);
    }
}