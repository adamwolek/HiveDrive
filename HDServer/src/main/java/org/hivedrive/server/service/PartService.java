package org.hivedrive.server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.config.ConfigurationService;
import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.mappers.PartMapper;
import org.hivedrive.server.repository.PartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class PartService {

	@Autowired
	private PartRepository partRepository;
	
	@Autowired
	private PartMapper mapper;
	
	@Autowired
	ServerConfigService serverConfigService;

	public PartEntity saveOrUpdate(PartTO to) {
		PartEntity existingPart = partRepository.findPart(to.getOwnerId(), to.getRepository(), to.getGroupId(), to.getOrderInGroup());
		if(existingPart == null) {
			PartEntity entity = mapper.map(to);
			return partRepository.save(entity);
		} else {
			//tutaj raczej powinien być rzucony wyjątek że obiekt już istnieje
			return existingPart;
		}
	}

	public boolean isAbleToAdd(PartTO part) {
		return true;
	}

	public boolean isAbleToUpdate(PartTO part) {
		return true;
	}
	
	public PartTO get(Long partId) {
		PartEntity partEntity = partRepository.findById(partId).get();
		return Optional.ofNullable(partEntity)
		.map(partOpt -> mapper.map(partOpt))
		.orElse(null);
	}

	public PartTO get(String ownerId, String repository, String groupId, Integer orderInGroup) {
		PartEntity part = partRepository.findPart(ownerId, repository, groupId, orderInGroup);
		return mapper.map(part);
	}
	
	public List<PartTO> findAllParts(){
		List<PartEntity> parts = Lists.newArrayList(partRepository.findAll());
		return mapper.mapToTOs(parts);
	}
	
	public File createFileForPart(PartEntity part, byte[] bytes) {
		try {
			if(part.getPathToPart() != null && part.getPathToPart().exists()) {
				FileUtils.forceDelete(part.getPathToPart());
				part.setPathToPart(null);
			}
			File location = serverConfigService.getLocationsWhereYouCanSaveFiles().get(0);
			File partFile = new File(location, part.getId() + "-" + part.getGlobalId());
			FileUtils.writeByteArrayToFile(partFile, bytes);
			part.setPathToPart(partFile);
			partRepository.save(part);
			return partFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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

}
