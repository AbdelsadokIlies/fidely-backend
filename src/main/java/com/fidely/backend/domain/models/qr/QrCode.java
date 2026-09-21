package com.fidely.backend.domain.models.qr;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente un QR code associé à un marchand.
 *
 * <p>Un QR code possède un type, un code et peut référencer une ressource
 * spécifique du domaine.</p>
 */
public class QrCode {

    private final UUID id;
    private final UUID merchantId;
    private final QrCodeType type;
    private final String code;
    private final UUID targetReferenceId;
    private final LocalDateTime createdAt;

    /**

     * Crée un nouveau QR code.
     *
     * @param id identifiant du QR code
     * @param merchantId id du marchand auquel le QR code est associé
     * @param type type du QR code
     * @param code valeur du QR code
     * @param targetReferenceId identifiant de la ressource référencée par le QR code
     * @param createdAt date de création du QR code
     */
    public QrCode(
            UUID id,
            UUID merchantId,
            QrCodeType type,
            String code,
            UUID targetReferenceId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.type = type;
        this.code = code;
        this.targetReferenceId = targetReferenceId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public QrCodeType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public UUID getTargetReferenceId() {
        return targetReferenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
