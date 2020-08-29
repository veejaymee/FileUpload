package com.qatar.bank.service;

import com.qatar.bank.exceptions.FileStoreException;
import com.qatar.bank.model.FileEntity;
import com.qatar.bank.repository.FileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStoreService {

    private final Path fileStorageLocation;

    @Autowired
    FileEntityRepository fileEntityRepository;

    @Autowired
    public FileStoreService(FileEntity fileEntity) {
        this.fileStorageLocation = Paths.get(fileEntity.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStoreException("Could not create the directory where the uploaded files will be stored.", ex);
        }

    }

    public String storeFile(MultipartFile file, Integer userId, String docType) {

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = "";

        try {
            if(originalFileName.contains("..")) {
                throw new FileStoreException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }
            String fileExtension = "";

            try {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } catch(Exception e) {
                fileExtension = "";
            }
            fileName = userId + "_" + docType + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            FileEntity doc = fileEntityRepository.checkDocumentByUserId(userId, docType);

            if(doc != null) {
                doc.setDocumentFormat(file.getContentType());
                doc.setFileName(fileName);
                fileEntityRepository.save(doc);
            } else {
                FileEntity newDoc = new FileEntity();
                newDoc.setUserId(userId);
                newDoc.setDocumentFormat(file.getContentType());
                newDoc.setFileName(fileName);
                newDoc.setDocumentType(docType);
                fileEntityRepository.save(newDoc);
            }

            return fileName;

        } catch (IOException ex) {
            throw new FileStoreException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }

        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName);
        }
    }

    public String getDocumentName(Integer userId, String docType) {
        return fileEntityRepository.getUploadDocumnetPath(userId, docType);
    }

    public FileEntity deleteFileByUserId(Integer userId) {
        return fileEntityRepository.deleteByUserId(userId);
    }

}
