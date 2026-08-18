package myspace_backend.dto.request;

import lombok.Data;
import myspace_backend.entity.OrganismeType;

@Data
public class FactureConsultationRequest {
    private OrganismeType organisme;
    private String referenceFacture;
}