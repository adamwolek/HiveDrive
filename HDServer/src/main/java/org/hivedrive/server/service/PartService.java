package org.hivedrive.server.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.mappers.PartMapper;
import org.hivedrive.server.repository.PartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PartService {

	@Autowired
	private PartRepository repository;

	@Autowired
	private PartMapper mapper;

	public PartEntity saveOrUpdate(PartTO to) {
		// + zapisać na dysku
		PartEntity entity = mapper.map(to);
		return repository.save(entity);
	}
	private final Path fileStorageLocation;

	public String storeFile(MultipartFile file, Integer userId, String docType) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = "";

        try {
            // Check if the file's name contains invalid characters
            if(originalFileName.contains("..")) {
                throw new DocumentStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            String fileExtension = "";
            try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } catch(Exception e) {
                fileExtension = "";
            }
            fileName = userId + "_" + docType + fileExtension;
         // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            PartEntity doc = docStorageRepo.checkDocumentByUserId(userId, docType);
            if(doc != null) {
                doc.setDocumentFormat(file.getContentType());
                doc.setFileName(fileName);
                docStorageRepo.save(doc);
            } else {
            	PartEntity newDoc = new DocumnentStorageProperties();
                newDoc.setUserId(userId);
                newDoc.setDocumentFormat(file.getContentType());
                newDoc.setFileName(fileName);
                newDoc.setDocumentType(docType);
                docStorageRepo.save(newDoc);
            }

            return fileName;
        } catch (IOException ex) {
            throw new DocumentStorageException("Could not store file " + fileName + ". Please try again!", ex);
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
        return docStorageRepo.getUploadDocumnetPath(userId, docType);
    }

	public boolean isAbleToAdd(PartTO part) {
		return true;
	}

	public boolean isAbleToUpdate(PartTO part) {
		return true;
	}

}
