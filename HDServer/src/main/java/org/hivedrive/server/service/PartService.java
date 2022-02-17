package org.hivedrive.server.service;

import java.io.File;
import java.util.ArrayList;
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
	private ConfigurationService configurationService;

	public PartEntity saveOrUpdate(PartTO to) {
		PartEntity entity = mapper.map(to);
		return partRepository.save(entity);
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
		ArrayList<PartEntity> parts = Lists.newArrayList(partRepository.findAll());
		PartEntity part = partRepository.findPart(ownerId, repository, groupId, orderInGroup);
		return mapper.map(part);
	}
	
	public List<PartTO> findAllParts(){
		List<PartEntity> parts = Lists.newArrayList(partRepository.findAll());
		return mapper.mapToTOs(parts);
	}
	
	public void createFileForPart(PartEntity part, byte[] bytes) {
		try {
			String path = configurationService.getLocationsWhereYouCanSaveFiles().get(0);
			File partFile = new File(path, part.getId() + "-" + part.getGlobalId());
			FileUtils.writeByteArrayToFile(partFile, bytes);
			part.setPathToPart(partFile);
			partRepository.save(part);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public File getFile(PartTO partTO) {
		String path = partTO.getRepository();
		return new File(path);
	}

}
