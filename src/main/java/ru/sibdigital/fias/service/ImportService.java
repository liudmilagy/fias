package ru.sibdigital.fias.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ImportService {

    String importData(File file);

    void importTestData();
}
