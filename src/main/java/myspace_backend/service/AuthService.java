package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.ChangerMotDePasseRequest;
import myspace_backend.dto.request.LoginRequest;
import myspace_backend.dto.request.VerifyOtpRequest;
import myspace_backend.dto.response.LoginInitResponse;
import myspace_backend.dto.response.LoginResponse;
import myspace_backend.entity.CodeOtp;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.CodeOtpInvalideException;
import myspace_backend.exception.CodeOtpVerrouilleException;
import myspace_backend.exception.IdentifiantsInvalidesException;
import myspace_backend.repository.CodeOtpRepository;
import myspace_backend.repository.UtilisateurRepository;
import myspace_backend.security.JwtUtil;
import myspace_backend.dto.request.ForgotPasswordInitRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_TENTATIVES_OTP = 3;

    private final UtilisateurRepository utilisateurRepository;
    private final CodeOtpRepository codeOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OtpTentativeService otpTentativeService;

    /**
     * Étape 1 du Login (2FA) : Vérification des identifiants & Génération d'OTP
     */
    @Transactional
    public LoginInitResponse initialiserConnexion(LoginRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByIdentifiantOrEmail(request.getIdentifiant(), request.getIdentifiant())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Identifiant ou mot de passe incorrect"));

        // 🔒 VÉRIFICATION COMPTE BLOQUÉ
        if (!utilisateur.isActif()) {
            throw new IdentifiantsInvalidesException("Votre compte a été suspendu par l'administration.");
        }

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IdentifiantsInvalidesException("Identifiant ou mot de passe incorrect");
        }

        codeOtpRepository.invaliderAnciensOtpsUtilisateur(utilisateur.getId());

        String codeOtp = genererCodeOtp();

        CodeOtp otpEntity = CodeOtp.builder()
                .code(codeOtp)
                .utilisateur(utilisateur)
                .dateExpiration(LocalDateTime.now().plusMinutes(5))
                .utilise(false)
                .build();

        codeOtpRepository.save(otpEntity);

        emailService.envoyerEmailOtp(utilisateur.getEmail(), codeOtp);

        return new LoginInitResponse(
                "Code de vérification envoyé par email.",
                utilisateur.getEmail(),
                utilisateur.isPremiereConnexion()
        );
    }

    /**
     * Étape 2 du Login (2FA) : Validation du code OTP & Génération JWT / Détection 1ère Connexion
     */
    @Transactional
    public LoginResponse verifierOtpEtConnecter(VerifyOtpRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        CodeOtp otpEntity = recupererOtpValide(utilisateur);
        validerCodeOtp(otpEntity, request.getCode());

        if (utilisateur.isPremiereConnexion()) {
            return new LoginResponse(
                    null,
                    true,
                    "Code OTP validé. Première connexion détectée, changement de mot de passe requis."
            );
        }

        String token = jwtUtil.genererToken(utilisateur.getEmail(), utilisateur.getRole().name());
        return new LoginResponse(token, false, "Connexion réussie");
    }

    /**
     * Changement du mot de passe temporaire lors de la 1ère connexion
     */
    @Transactional
    public void changerMotDePasse(ChangerMotDePasseRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IdentifiantsInvalidesException("L'ancien mot de passe est incorrect");
        }

        if (request.getAncienMotDePasse().equals(request.getNouveauMotDePasse())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien mot de passe.");
        }

        if (request.getConfirmerMotDePasse() == null || !request.getNouveauMotDePasse().equals(request.getConfirmerMotDePasse())) {
            throw new IllegalArgumentException("Le nouveau mot de passe et sa confirmation ne correspondent pas.");
        }

        validerComplexiteMotDePasse(request.getNouveauMotDePasse());

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setPremiereConnexion(false);

        utilisateurRepository.save(utilisateur);
    }

    // --- Utilitaires de sécurité ---

    private String genererCodeOtp() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private void validerComplexiteMotDePasse(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une lettre majuscule.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre.");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*...).");
        }
    }

    /**
     * Étape 1 du Mot de Passe Oublié : Vérification Identifiant/Email & Génération OTP
     */
    @Transactional
    public void initialiserMotDePasseOublie(ForgotPasswordInitRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByIdentifiantOrEmail(request.getIdentifiant(), request.getIdentifiant())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Informations incorrectes."));

        if (!utilisateur.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IdentifiantsInvalidesException("Informations incorrectes.");
        }

        if (!utilisateur.isActif()) {
            throw new IdentifiantsInvalidesException("Votre compte a été suspendu par l'administration.");
        }

        codeOtpRepository.invaliderAnciensOtpsUtilisateur(utilisateur.getId());

        String codeOtp = genererCodeOtp();

        CodeOtp otpEntity = CodeOtp.builder()
                .code(codeOtp)
                .utilisateur(utilisateur)
                .dateExpiration(LocalDateTime.now().plusMinutes(5))
                .utilise(false)
                .build();

        codeOtpRepository.save(otpEntity);

        emailService.envoyerEmailOtp(utilisateur.getEmail(), codeOtp);
    }

    /**
     * Étape 2 du Mot de Passe Oublié : Validation OTP & Envoi Identifiant + MDP Temporaire
     */
    @Transactional
    public void validerMotDePasseOublieEtEnvoyerMdp(VerifyOtpRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        CodeOtp otpEntity = recupererOtpValide(utilisateur);
        validerCodeOtp(otpEntity, request.getCode());

        String mdpTemporaire = genererMotDePasseTemporaire();
        utilisateur.setMotDePasse(passwordEncoder.encode(mdpTemporaire));
        utilisateur.setPremiereConnexion(true);

        utilisateurRepository.save(utilisateur);

        codeOtpRepository.invaliderAnciensOtpsUtilisateur(utilisateur.getId());

        String contenuEmail = String.format(
                "Bonjour,\n\nVoici vos identifiants pour vous connecter à My Space :\n\n" +
                        "Identifiant : %s\n" +
                        "Mot de passe temporaire : %s\n\n" +
                        "Lors de votre connexion, vous serez invité à définir un nouveau mot de passe.",
                utilisateur.getIdentifiant(),
                mdpTemporaire
        );

        emailService.envoyerEmailOtp(utilisateur.getEmail(), contenuEmail);
    }

    private String genererMotDePasseTemporaire() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Génération & Envoi OTP pour un Virement vers un Tiers
     */
    @Transactional
    public void genererEtEnvoyerOtpVirement(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        codeOtpRepository.invaliderAnciensOtpsUtilisateur(utilisateur.getId());

        String codeOtp = genererCodeOtp();

        CodeOtp otpEntity = CodeOtp.builder()
                .code(codeOtp)
                .utilisateur(utilisateur)
                .dateExpiration(LocalDateTime.now().plusMinutes(5))
                .utilise(false)
                .build();

        codeOtpRepository.save(otpEntity);

        emailService.envoyerEmailOtp(utilisateur.getEmail(), codeOtp);
    }

    /**
     * Validation OTP pour Virement / Cartes
     */
    @Transactional
    public void validerOtpVirement(String email, String code) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        CodeOtp otpEntity = recupererOtpValide(utilisateur);
        validerCodeOtp(otpEntity, code);
    }

    // --- OTP : récupération & validation centralisées ---

    private CodeOtp recupererOtpValide(Utilisateur utilisateur) {
        return codeOtpRepository.findTopByUtilisateurAndUtiliseFalseOrderByDateExpirationDesc(utilisateur)
                .orElseThrow(() -> new CodeOtpInvalideException("Aucun code OTP valide n'a été trouvé"));
    }

    private void validerCodeOtp(CodeOtp otpEntity, String codeSaisi) {

        if (otpEntity.isVerrouille()) {
            throw new CodeOtpVerrouilleException(
                    "Ce code a été verrouillé suite à trop de tentatives incorrectes. Veuillez redemander un nouveau code."
            );
        }

        if (otpEntity.getDateExpiration().isBefore(LocalDateTime.now())) {
            otpTentativeService.marquerExpire(otpEntity.getId());
            throw new CodeOtpInvalideException("Le code OTP a expiré");
        }

        if (!otpEntity.getCode().equals(codeSaisi)) {
            int tentatives = otpEntity.getAttempts() + 1;

            if (tentatives >= MAX_TENTATIVES_OTP) {
                otpTentativeService.enregistrerTentativeEchouee(otpEntity.getId(), tentatives, true);
                throw new CodeOtpVerrouilleException(
                        "Trop de tentatives incorrectes. Veuillez redemander un nouveau code."
                );
            }

            otpTentativeService.enregistrerTentativeEchouee(otpEntity.getId(), tentatives, false);
            int restantes = MAX_TENTATIVES_OTP - tentatives;
            throw new CodeOtpInvalideException(
                    "Code OTP incorrect. Il vous reste " + restantes + " tentative(s)."
            );
        }

        otpEntity.setUtilise(true);
        codeOtpRepository.save(otpEntity);
    }
}