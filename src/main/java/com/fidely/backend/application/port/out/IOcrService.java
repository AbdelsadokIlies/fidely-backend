package com.fidely.backend.application.port.out;

import com.fidely.backend.infrastructure.ocr.OcrTicketData;

public interface IOcrService {

    OcrTicketData extractTicketData(byte[] image);
}