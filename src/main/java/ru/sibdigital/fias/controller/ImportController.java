package ru.sibdigital.fias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.sibdigital.fias.service.ImportService;

import java.io.File;
import java.io.IOException;

@Controller
public class ImportController {

    @Autowired
    private ImportService importService;

    @GetMapping("/import_test")
    public ResponseEntity<String> importTestData() {
        importService.importTestData();
        return ResponseEntity.ok().body("Ok");
    }

    @PostMapping("/process_file")
    public @ResponseBody
    String processFile(@RequestParam(name = "file") MultipartFile multipartFile) throws IOException {
        String tmpPath = System.getProperty("java.io.tmpdir");
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            tmpPath += "\\";
        }
        else {
            tmpPath += "/";
        }
        File file = new File(tmpPath + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return importService.importData(file);
    }
}
