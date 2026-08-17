package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.BeneficiaireRequest;
import myspace_backend.dto.response.BeneficiaireResponse;
import myspace_backend.entity.Beneficiaire;
import myspace_backend.entity.Client;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.IdentifiantsInvalidesException;
import myspace_backend.repository.BeneficiaireRepository;
import myspace_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeneficiaireService {

    private final BeneficiaireRepository beneficiaireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<BeneficiaireResponse> listerBeneficiaires(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        return beneficiaireRepository.findByClient(utilisateur.getClient()).stream()
                .map(b -> new BeneficiaireResponse(b.getId(), b.getNom(), b.getRib()))
                .collect(Collectors.toList());
    }

    @Transactional
    public BeneficiaireResponse ajouterBeneficiaire(String email, BeneficiaireRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        // 1. Validate OTP for adding new RIB
        authService.validerOtpVirement(email, request.getCodeOtp());

        Client client = utilisateur.getClient();

        // 2. Prevent duplicate RIBs for the same client
        beneficiaireRepository.findByClientAndRib(client, request.getRib())
                .ifPresent(b -> {
                    throw new IdentifiantsInvalidesException("Ce RIB est déjà enregistre dans vos bénéficiaires.");
                });

        // 3. Save new Beneficiaire
        Beneficiaire beneficiaire = Beneficiaire.builder()
                .nom(request.getNom())
                .rib(request.getRib())
                .client(client)
                .build();

        Beneficiaire saved = beneficiaireRepository.save(beneficiaire);
        return new BeneficiaireResponse(saved.getId(), saved.getNom(), saved.getRib());
    }

    @Transactional
    public void supprimerBeneficiaire(String email, Long id) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        Beneficiaire beneficiaire = beneficiaireRepository.findById(id)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Bénéficiaire introuvable."));

        if (!beneficiaire.getClient().getId().equals(utilisateur.getClient().getId())) {
            throw new IdentifiantsInvalidesException("Action non autorisée.");
        }

        beneficiaireRepository.delete(beneficiaire);
    }
}