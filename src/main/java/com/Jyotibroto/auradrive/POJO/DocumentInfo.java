package com.Jyotibroto.auradrive.POJO;

import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.enums.DocumentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentInfo {
    private DocumentType documentType;
    private String url;
    private LocalDateTime uploadedAt;
}
