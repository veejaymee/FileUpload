package com.qatar.bank.controller;

import com.qatar.bank.model.FileEntity;
import com.qatar.bank.response.FileUploadResponse;
import com.qatar.bank.service.FileStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class FileController {

    @Autowired
    private FileStoreService fileStoreService;

    @PostMapping("/uploadFile")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("userId") Integer UserId,
                                         @RequestParam("docType") String docType) {
        String fileName = fileStoreService.storeFile(file, UserId, docType);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/viewFile/")
                .path(fileName)
                .toUriString();
        return new FileUploadResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @GetMapping("/viewFile")
    public ResponseEntity<Resource> viewFile(@RequestParam("userId") Integer userId,
                                                 @RequestParam("docType") String docType,
                                                 HttpServletRequest request) {

        String fileName = fileStoreService.getDocumentName(userId, docType);
        Resource resource = null;
        if (fileName != null && !fileName.isEmpty()) {
            try {
                resource = fileStoreService.loadFileAsResource(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                //logger.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined

            if (contentType == null) {
                contentType = "application/json";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } else {
            return ResponseEntity.notFound().build();

        }
    }

    @PostMapping("/deleteFileByUserId")
    public FileUploadResponse deleteFileByUserId(@RequestParam("userId") Integer userId) {
        FileEntity fileEntity = fileStoreService.deleteFileByUserId(userId);

        return new FileUploadResponse(fileEntity.getFileName(), fileEntity.getUploadDir(),
                fileEntity.getDocumentType(), 1l);
    }

}
