package myspace_backend.dto.request;

import lombok.Data;
import myspace_backend.entity.OrganismeType;

@Data
public class PaiementFactureRequest {
    private String ribSource;
    private OrganismeType organisme;
    private String referenceFacture;
    private String codeOtp;
}