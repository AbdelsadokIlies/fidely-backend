package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketRequest;
import com.fidely.backend.api.dtos.mappers.tickets.OcrTicketResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.CreateTransactionResponseMapper;
import com.fidely.backend.api.dtos.mappers.transactions.ManualTransactionRequestMapper;
import com.fidely.backend.api.dtos.models.tickets.OcrTicketResponse;
import com.fidely.backend.api.dtos.models.transactions.CreateTransactionResponse;
import com.fidely.backend.api.dtos.models.transactions.ManualTransactionRequest;
import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.domain.models.users.MerchantManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du controller REST des transactions.
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private ITicketService ticketService;

    @Mock
    private ILoyaltyService loyaltyService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private OcrTicketResponseMapper ocrTicketResponseMapper;

    @Mock
    private ManualTransactionRequestMapper manualTransactionRequestMapper;

    @Mock
    private CreateTransactionResponseMapper createTransactionResponseMapper;

    @Mock
    private MerchantManager merchantManager;

    @InjectMocks
    private TransactionController transactionController;

    /**
     * Vérifie que le controller transmet correctement les données
     * au service OCR et retourne la réponse correspondante.
     */
    @Test
    void shouldExtractTicketFromOcr() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        byte[] imageContent = "fake-image".getBytes();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "ticket.jpg",
                "image/jpeg",
                imageContent
        );

        Ticket ticket = new Ticket(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.PENDING,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );

        OcrTicketResponse response = new OcrTicketResponse(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                TicketStatus.PENDING
        );

        when(ticketService.extractTicketFromOcr(
                imageContent,
                merchantId,
                customerId
        )).thenReturn(ticket);

        when(ocrTicketResponseMapper.toResponse(ticket))
                .thenReturn(response);

        OcrTicketRequest request =
                new OcrTicketRequest(
                        merchantId,
                        customerId,
                        image
                );

        ResponseEntity<OcrTicketResponse> result =
                transactionController.extractTicketFromOcr(request);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isSameAs(response);

        verify(ticketService).extractTicketFromOcr(
                imageContent,
                merchantId,
                customerId
        );

        verify(ocrTicketResponseMapper).toResponse(ticket);
    }

    /**
     * Vérifie qu'un marchand peut enregistrer une transaction manuelle.
     */
    @Test
    void shouldCreateManualTransactionAsMerchant() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        loyaltyId,
                        merchantId,
                        customerId,
                        new BigDecimal("112.00")
                );

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                112,
                LocalDateTime.of(2026, 6, 15, 12, 30),
                LocalDateTime.of(2026, 6, 15, 12, 31)
        );

        CreateTransactionResponse response =
                new CreateTransactionResponse(
                        loyaltyId,
                        customerId,
                        merchantId,
                        112,
                        loyalty.getUpdatedAt()
                );

        Authentication authentication =
                createAuthentication(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(merchantManager.getMerchantId())
                .thenReturn(merchantId);

        when(manualTransactionRequestMapper.toRequest(request))
                .thenReturn(request);

        when(loyaltyService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                new BigDecimal("112.00")
        )).thenReturn(loyalty);

        when(createTransactionResponseMapper.toResponse(loyalty))
                .thenReturn(response);

        ResponseEntity<CreateTransactionResponse> result =
                transactionController.createManualTransaction(
                        request,
                        authentication
                );

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isSameAs(response);

        verify(loyaltyService).addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                new BigDecimal("112.00")
        );
    }

    /**
     * Vérifie qu'un utilisateur qui n'est pas marchand
     * ne peut pas enregistrer une transaction manuelle.
     */
    @Test
    void shouldRejectManualTransactionWhenUserIsNotMerchant() {
        UUID userId = UUID.randomUUID();

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("112.00")
                );

        Authentication authentication =
                createAuthentication(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionController.createManualTransaction(
                        request,
                        authentication
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "L'utilisateur authentifié n'est pas un marchand."
                );

        verifyNoInteractions(loyaltyService);
    }

    /**
     * Vérifie qu'un marchand ne peut pas enregistrer
     * une transaction pour un autre marchand.
     */
    @Test
    void shouldRejectManualTransactionForAnotherMerchant() {
        UUID userId = UUID.randomUUID();
        UUID authenticatedMerchantId = UUID.randomUUID();
        UUID requestMerchantId = UUID.randomUUID();

        ManualTransactionRequest request =
                new ManualTransactionRequest(
                        UUID.randomUUID(),
                        requestMerchantId,
                        UUID.randomUUID(),
                        new BigDecimal("112.00")
                );

        Authentication authentication =
                createAuthentication(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(merchantManager.getMerchantId())
                .thenReturn(authenticatedMerchantId);

        when(manualTransactionRequestMapper.toRequest(request))
                .thenReturn(request);

        assertThatThrownBy(() ->
                transactionController.createManualTransaction(
                        request,
                        authentication
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Le marchand fourni ne correspond pas au marchand authentifié."
                );

        verifyNoInteractions(loyaltyService);
    }

    /**
     * Crée une authentification simulée contenant l'identifiant
     * utilisateur attendu par le controller.
     *
     * @param userId identifiant de l'utilisateur
     * @return authentification simulée
     */
    private Authentication createAuthentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null
        );
    }
}