package ru.sibdigital.fias.service;

import java.io.File;

public interface ImportService {

    String importData(File file);

    void importTestData();
}
