package org.hivedrive.server.entity;

import java.io.File;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class PartEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private NodeEntity node;
	
	
	private File pathToPart;
	private LocalDateTime createDate;
	
	
	
}
