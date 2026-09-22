package com.fidely.backend.application.port.out;

import com.fidely.backend.infrastructure.ocr.OcrTicketData;

/**
 * Port de sortie permettant d'extraire les données d'un ticket
 * à partir de son image via un service OCR.
 */
public interface IOcrService {

    OcrTicketData extractTicketData(byte[] image);
}