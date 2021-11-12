package org.hivedrive.server.to;

import java.io.File;
import java.time.LocalDateTime;

public class PartTO {

	private LocalDateTime createDate;
	private String status;
	private String globalId;
	/**
	 * Name of repository created by repository owner
	 */
	private String repository;
	/**
	 * Encrypted name of the file from which the part was extracted
	 */
	private String groupId;
	private int orderInGroup;
	private String ownerId;
}
