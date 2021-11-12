package org.hivedrive.server.to;

import java.io.File;
import java.time.LocalDateTime;

public class PartTO {

	private File pathToPart;
	private LocalDateTime createDate;
	private String status;
	private String globalId;
	private String repository;
	private String groupId;
	private int orderInGroup;
	private String ownerId;
}
