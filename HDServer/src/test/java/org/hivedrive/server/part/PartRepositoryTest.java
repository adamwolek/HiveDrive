package org.hivedrive.server.part;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.hivedrive.server.entity.PartEntity;
import org.hivedrive.server.repository.PartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
@DisplayName("PartRepositoryTest")
public class PartRepositoryTest {

	@Autowired
	private PartRepository partRepository;
	
	@Test
	@DisplayName("Test summing size of saved parts")
	void summingSizeOfSavedParts() {
		Random random = new Random();
		
		for (int i = 0; i < 100; i++) {
			PartEntity part = new PartEntity();
			part.setPathToPart("/nonSummed/" + random.nextInt(100000));
			part.setSpaceId("/nonSummed");
			part.setSize(random.nextInt(10000) + 1000);
			partRepository.save(part);
		}
		
		long sizeSum = 0;
		for (int i = 0; i < 100; i++) {
			PartEntity part = new PartEntity();
			part.setPathToPart("/summed/" + random.nextInt(100000));
			part.setSpaceId("/summed");
			part.setSize(random.nextInt(10000) + 1000);
			sizeSum += part.getSize();
			partRepository.save(part);
		}
		
		assertEquals(sizeSum, partRepository.sizeForPath("/summed"));
	}
	
}
