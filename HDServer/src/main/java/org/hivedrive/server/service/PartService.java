package org.hivedrive.server.service;

import java.util.ArrayList;
import java.util.List;

import org.hivedrive.cmd.to.PartTO;
import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.mappers.PartMapper;
import org.hivedrive.server.repository.PartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service
public class PartService {

	@Autowired
	private PartRepository partRepository;

	@Autowired
	private PartMapper mapper;

	public PartEntity saveOrUpdate(PartTO to) {
		// + zapisać na dysku
		PartEntity entity = mapper.map(to);
		return partRepository.save(entity);
	}

	public boolean isAbleToAdd(PartTO part) {
		return true;
	}

	public boolean isAbleToUpdate(PartTO part) {
		return true;
	}

	public PartTO get(String ownerId, String repository, String groupId, Integer orderInGroup) {
		PartTO part = partRepository.findPart(ownerId, repository, groupId, orderInGroup);
		return part;
	}
	
	public List<PartTO> findAllParts(){
		List<PartEntity> parts = Lists.newArrayList(partRepository.findAll());
		return mapper.mapToTOs(parts);
	}

}
