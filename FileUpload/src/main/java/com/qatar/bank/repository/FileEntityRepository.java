package com.qatar.bank.repository;

import com.qatar.bank.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileEntityRepository extends JpaRepository<FileEntity, Integer> {

    @Query("Select a from FileEntity a where user_id = ? and document_type = ?")
    FileEntity checkDocumentByUserId(Integer userId, String docType);

    @Query("Select fileName from FileEntity a where user_id = ? and document_type = ?")
    String getUploadDocumnetPath(Integer userId, String docType);

    @Query("Delete from FileEntity a where user_id = ?")
    FileEntity deleteByUserId(Integer userId);
}