package org.hivedrive.cmd.integration;

import static org.hivedrive.cmd.helper.LocalRepoOperationsHelper.fileHash;
import static org.hivedrive.cmd.helper.LocalRepoOperationsHelper.getAllFiles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.main.MainApplicationRunner;
import org.hivedrive.cmd.tool.FileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("unitTests")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class MainIntegrationTest {

	@TempDir
	public File keysFolder;
	
	@TempDir
	public File repoA;
	
	@TempDir
	public File repoB;
	
	@Autowired
	public MainApplicationRunner runner;
	
	@Test
	public void test() throws IOException {
		File keys = generateKeys();
		init(keys, repoA);
		init(keys, repoB);
		fillRepository(repoA);
		push(repoA);
		pull(repoB);
		assertContainsSameFiles(repoA, repoB);
		
	}

	private File generateKeys() {
		File keys = new File(keysFolder, "keys");
		runner.runCommand("generateKeys " + keys.getAbsolutePath());
		return keys;
	}

	private void assertContainsSameFiles(File repoA, File repoB) {
		Collection<File> filesFromA = getAllFiles(repoA);
		Collection<File> filesFromB = getAllFiles(repoB);
		assertEquals(filesFromA.size(), filesFromB.size());
		
		for (File fileA : filesFromA) {
			File fileB = findSameGlobalId(fileId(fileA, repoA), repoB);
			assertNotNull(fileB);
			assertEquals(fileHash(fileA), fileHash(fileB));
		}
		
	}

	private File findSameGlobalId(String globalId, File repo) {
		for (File file : getAllFiles(repo)) {
			if(globalId.equals(fileId(file, repo))) {
				return file;
			}
		}
		return null;
	}

	private void init(File keys, File repo) {
		runner.runCommand(String.format("init --key=%s --directory=%s", 
				keys.getAbsolutePath(), repo.getAbsolutePath()));
	}
	
	private void push(File repo) {
		runner.runCommand("push --directory=" + repo.getAbsolutePath());
	}
	
	private void pull(File repo) {
		runner.runCommand("pull --directory=" + repo.getAbsolutePath());
		
	}

	private List<File> fillRepository(File repo) {
		return Arrays.asList(newFileIn(repo), newFileIn(repo), newFileIn(repo));
	}

	private File newFileIn(File repo) {
		File file = new File(repoA, RandomStringUtils.random(10));
		FileGenerator.createBigFile(file);
		return file;
	}
	
	private String fileId(File file, File repo) {
		return repo.toURI().relativize(file.toURI()).getPath();
	}
	
}
