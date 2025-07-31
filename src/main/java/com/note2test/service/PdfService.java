package com.note2test.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfService {
    public String extractText(MultipartFile file) throws Exception {
        Tika tika = new Tika();
        return tika.parseToString(file.getInputStream());
    }
}
