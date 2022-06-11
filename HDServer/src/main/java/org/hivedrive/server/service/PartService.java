package org.hivedrive.server.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.controller.SenderInfo;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.exception.NotEnaughSpaceException;
import org.hivedrive.server.mappers.PartMapper;
import org.hivedrive.server.repository.PartRepository;
import org.hivedrive.server.saving.SpaceForSave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service
public class PartService {

	@Autowired
	private PartRepository partRepository;
	
	@Autowired
	private PartMapper mapper;
	
	@Autowired
	private ServerConfigService serverConfigService;

	public PartEntity saveOrUpdate(PartTO to) {
		PartEntity existingPart = partRepository.findPart(
				to.getOwnerId(), 
				to.getRepository(), 
				to.getGroupId(), 
				to.getOrderInGroup());
		if(existingPart == null) {
			PartEntity entity = mapper.map(to);
			entity.setCreateDate(LocalDateTime.now());
			return partRepository.save(entity);
		} else {
			//tutaj raczej powinien być rzucony wyjątek że obiekt już istnieje
			return existingPart;
		}
	}
	
	public boolean isAbleToDelete(PartEntity part, SenderInfo senderInfo) {
		return part.getNode().getPublicKey().equals(senderInfo.getSenderPublicKey());
	}

	public boolean isAbleToAdd(PartTO part, SenderInfo senderInfo) {
		//TODO: Just implement;
		return true;
	}

	public boolean isAbleToUpdate(PartTO part, SenderInfo senderInfo) {
		return part.getOwnerId().equals(senderInfo.getSenderPublicKey());
	}
	
	public PartTO get(Long partId) {
		PartEntity partEntity = partRepository.findById(partId).get();
		return Optional.ofNullable(partEntity)
		.map(partOpt -> mapper.map(partOpt))
		.orElse(null);
	}

	public PartTO get(String ownerId, String repository, String groupId, Integer orderInGroup) {
		PartEntity part = partRepository.findPart(ownerId, repository, groupId, orderInGroup);
		System.out.println("ownerId: " + ownerId + "\n" +
				"repository: " + repository + "\n" +
				"groupId: " + groupId + "\n" +
				"orderInGroup: " + orderInGroup);
		if(part == null) {
			return null;
		}
		return mapper.map(part);
	}
	
	public boolean doesExist(String fileHash) {
		Long howMany = partRepository.howManyPartsExists(fileHash);
		return howMany > 0;
	}
	
	public List<PartTO> findAllParts(){
		List<PartEntity> parts = Lists.newArrayList(partRepository.findAll());
		return mapper.mapToTOs(parts);
	}
	
	public File createFileForPart(PartEntity part, byte[] bytes) {
		try {
			if(part.getPathToPart() != null && new File(part.getPathToPart()).exists()) {
				FileUtils.forceDelete(new File(part.getPathToPart()));
				part.setPathToPart(null);
				part.setSpaceId(null);
				part.setSize(0);
			}
			SpaceForSave bestSpace = findLocationForNewPart(DataSize.ofBytes(bytes.length));
			File partFile = new File(bestSpace.getDirectory(), part.getId() + "-" + part.getGlobalId());
			FileUtils.writeByteArrayToFile(partFile, bytes);
			part.setPathToPart(partFile.getAbsolutePath());
			part.setSpaceId(bestSpace.getDirectory().getAbsolutePath());
			part.setSize(bytes.length);
			partRepository.save(part);
			return partFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private SpaceForSave findLocationForNewPart(DataSize partSize) {
		SpaceForSave first = Iterables.getFirst(serverConfigService.getSpacesForSave(), null);
		DataSize size = first.getSize();
		DataSize spaceLeft = first.spaceLeft();
		Optional<SpaceForSave> spaceForSave = serverConfigService.getSpacesForSave().stream()
		.filter(space -> space.spaceLeft().compareTo(partSize) > 0)
		.findFirst();
		if(spaceForSave.isPresent()) {
			return spaceForSave.get();
		} else {
			throw new NotEnaughSpaceException();
		}
	}

	public File getFile(PartTO partTO) {
		String path = partTO.getRepository();
		return new File(path);
	}

	public List<PartTO> get(String senderPublicKey, String repository) {
		Collection<PartEntity> parts = partRepository.findPart(senderPublicKey, repository);
		return mapper.mapToTOs(parts);
	}

	public boolean delete(Long partId) {
		partRepository.deleteById(partId);
		return true;
	}


}
