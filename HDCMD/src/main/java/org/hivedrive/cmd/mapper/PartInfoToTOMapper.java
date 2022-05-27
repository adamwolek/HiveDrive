package org.hivedrive.cmd.mapper;

import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.to.PartTO;

public class PartInfoToTOMapper {

	public static PartInfoToTOMapper create() {
		return new PartInfoToTOMapper();
	}

	public PartTO map(PartInfo part) {
		PartTO to = new PartTO();
		to.setGroupId(part.getFileMetadata().getFileId());
		to.setOrderInGroup(part.getFileMetadata().getPartIndex());
		to.setRepository(part.getFileMetadata().getRepository());
		to.setOwnerId(part.getOwnerPublicKey());
		to.setEncryptedFileMetadata(part.getEncryptedFileMetadata());
		to.setFileHash(part.getFileHash());
		return to;
	}
	
}
