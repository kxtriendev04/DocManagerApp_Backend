package com.vn.document.controller;

import com.vn.document.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @Value("${upload-file.base-uri}")
    private String baseUri;


    @PostMapping
    public ResponseEntity<String> postFile(@RequestPart("file") MultipartFile file,
                                           @RequestParam("folder") String folder,
                                           @RequestParam("password") String password) throws URISyntaxException, IOException {
//        Validate
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File trống.");
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Mật khẩu không được để trống.");
        }
//        Create direction if not exitst
//        fileService.createDirectory(baseUri + "/" + folder);
//        String path = Paths.get(baseUri, folder).toString();
        fileService.createDirectory(folder);
//        Store
        String responsePath = fileService.handleStoreFile(file, folder, password);
        return ResponseEntity.ok(responsePath);
    }

    @PostMapping("/multi")
    public ResponseEntity<List<String>> postMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                          @RequestParam("folder") String folder,
                                                          @RequestParam("password") String password) throws IOException {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }

        fileService.createDirectory(folder);
        List<String> filePaths = new ArrayList<>();
        for (MultipartFile file : files) {
            String saved = fileService.handleStoreFile(file, folder, password);
            filePaths.add(saved);
        }

        return ResponseEntity.ok(filePaths);
    }


    @GetMapping
    public ResponseEntity<Resource> downloadFile(@RequestParam String folder,
                                                 @RequestParam String filename,
                                                 @RequestParam String password) throws IOException {
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Resource file = fileService.loadFile(folder, filename, password);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

//    @GetMapping("/multi")
//    public ResponseEntity<List<String>> getMultipleFiles(@RequestParam("files") MultipartFile[] files,
//                                                         @RequestParam("folder") String folder) throws IOException {
//        fileService.createDirectory(folder);
//
//        List<String> fileNames = new ArrayList<>();
//        for (MultipartFile file : files) {
//            String saved = fileService.handleStoreFile(file, folder);
//            fileNames.add(saved);
//        }
//
//        return ResponseEntity.ok(fileNames);
//    }
}
