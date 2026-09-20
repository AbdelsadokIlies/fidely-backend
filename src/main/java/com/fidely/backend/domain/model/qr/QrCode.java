package com.fidely.backend.domain.model.qr;

import com.fidely.backend.domain.model.Merchants.Merchant;

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
    private final Merchant merchant;
    private final QrCodeType type;
    private final String code;
    private final UUID targetReferenceId;
    private final LocalDateTime createdAt;

    /**

     * Crée un nouveau QR code.
     *
     * @param id identifiant du QR code
     * @param merchant marchand auquel le QR code est associé
     * @param type type du QR code
     * @param code valeur du QR code
     * @param targetReferenceId identifiant de la ressource référencée par le QR code
     * @param createdAt date de création du QR code
     */
    public QrCode(
            UUID id,
            Merchant merchant,
            QrCodeType type,
            String code,
            UUID targetReferenceId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.merchant = merchant;
        this.type = type;
        this.code = code;
        this.targetReferenceId = targetReferenceId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
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
