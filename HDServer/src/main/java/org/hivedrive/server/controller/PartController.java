package org.hivedrive.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;

import io.swagger.models.Response;

@RestController
@RequestMapping("/part")
public class PartController {

	@Autowired
	private PartService service;
	
	public PartController(PartService service) {
		this.service = service;
	}
	
	@PostMapping
	public ResponseEntity<Void> post(@RequestBody PartTO part) {
		if (service.isAbleToAdd(part)) {
			PartEntity entity = service.saveOrUpdate(part);
			if (entity != null) {
				return new ResponseEntity<>(HttpStatus.CREATED);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	
	@PutMapping
	public ResponseEntity<Void> put(@RequestBody PartTO part) {
		if (service.isAbleToUpdate(part)) {
			PartEntity entity = service.saveOrUpdate(part);
			if (entity != null) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	
	@GetMapping("/{publicKey}/{groupId}/{orderInGroup}")
	PartTO get() {
		//znaleźć entity
		//znaleźć part na dysku i go wysłać
		
		return null;
	}
	
    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam("userId") Integer userId,
            @RequestParam("docType") String docType,
            HttpServletRequest request) {
        String fileName = service.getDocumentName(userId, docType);
        Resource resource = null;
        if(fileName !=null && !fileName.isEmpty()) {
        try {
            resource = service.loadFileAsResource(fileName);
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

        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    } else {
        return ResponseEntity.notFound().build();

    }
    }   

	@GetMapping("/all")
	List<PartTO> getAll() {
		
		return null;
	}
	
	@PostMapping(path = "/content", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	void postContent(@RequestPart MultipartFile content) {
		
	}
	
	@GetMapping(path = "/content", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	ResponseEntity<Resource> getContent() throws FileNotFoundException {
		File file = new File("");
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		return ResponseEntity.ok()
	            .contentLength(file.length())
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);
	}
	
}
