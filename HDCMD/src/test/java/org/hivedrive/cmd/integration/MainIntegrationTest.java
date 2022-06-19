package org.hivedrive.cmd.integration;


import static org.hivedrive.cmd.helper.RepoOperationsHelper.getAllFiles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hivedrive.cmd.config.TestConfig;
import org.hivedrive.cmd.helper.RepoOperationsHelper;
import org.hivedrive.cmd.main.MainApplicationRunner;
import org.hivedrive.cmd.tool.FileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Iterables;

@ActiveProfiles("unitTests")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class MainIntegrationTest {

	@TempDir
	static File tempFolder;
	
	@Autowired
	public MainApplicationRunner runner;

	private String repoName;
	
	@Autowired
	public RepoOperationsHelper repoOperationsHelper;
	
	@Test
	public void pushAndPull() throws IOException {
		File keys = generateKeys(tempFolder);
		repoName = "myRepo_" + randString();
		File repoA = createRepoDir();
		File repoB = createRepoDir();
		init(keys, repoA, repoName);
		init(keys, repoB, repoName);
		fillRepository(repoA);
		push(repoA);
		pull(repoB);
		assertContainsSameFiles(repoA, repoB);
	}
	
	@Test
	public void cleanPush() throws IOException {
		File keys = generateKeys(tempFolder);
		repoName = "myRepo_" + randString();
		File repoA = createRepoDir();
		File repoB = createRepoDir();
		init(keys, repoA, repoName);
		init(keys, repoB, repoName);
		Collection<File> filesInRepo = fillRepository(repoA);
		push(repoA);
		File removedFile = removeOneFile(filesInRepo);
		cleanPush(repoA);
		pull(repoB);
		assertContainsSameFiles(repoA, repoB);
		assertFalse(fileExists(repoB, removedFile));
	}
	
	@Test
	public void cleanPull() throws IOException {
		File keys = generateKeys(tempFolder);
		repoName = "myRepo_" + randString();
		File repoA = createRepoDir();
		File repoB = createRepoDir();
		init(keys, repoA, repoName);
		init(keys, repoB, repoName);
		fillRepository(repoA);
		push(repoA);
		pull(repoB);
		File newFile = newSmallFileIn(repoB);
		pull(repoB);
		assertTrue(fileExists(repoB, newFile));
		cleanPull(repoB);
		assertFalse(fileExists(repoB, newFile));
	}
	
	


	private boolean fileExists(File repoB, File newFile) {
		return getAllFiles(repoB).stream()
		.filter(file -> file.getName().equals(newFile.getName()))
		.anyMatch(file -> fileId(file, repoB).equals(fileId(newFile, repoB)));
	}

	private File removeOneFile(Collection<File> filesInRepo) throws IOException {
		File file = Iterables.getFirst(filesInRepo, null);
		FileUtils.forceDelete(file);
		return file;
	}

	private File createRepoDir() {
		File dir = new File(tempFolder, "repo_" + randString());
		dir.mkdir();
		return dir;
	}

	private File generateKeys(File tempFolder) {
		File keys = new File(tempFolder, "keys_" + randString());
		runner.runCommand("generateKeys " + keys.getAbsolutePath());
		return keys;
	}

	private void assertContainsSameFiles(File repoA, File repoB) {
		Collection<File> filesFromA = getAllFiles(repoA);
		Collection<File> filesFromB = getAllFiles(repoB);
		assertEquals(filesFromA.size(), filesFromB.size());
		for (File fileA : getAllFiles(repoA)) {
			File fileB = findSameGlobalId(fileId(fileA, repoA), repoB);
			assertNotNull(fileB);
			assertEquals(fileId(fileA, repoA), fileId(fileB, repoB));
		}
	}
	
	private String randString() {
		return RandomStringUtils.randomAlphabetic(15);
	}

	private File findSameGlobalId(String globalId, File repo) {
		for (File file : getAllFiles(repo)) {
			if(globalId.equals(fileId(file, repo))) {
				return file;
			}
		}
		return null;
	}

	private void init(File keys, File repo, String repoName) {
		runner.runCommand(String.format("init --key=%s --directory=%s --name=%s", 
				keys.getAbsolutePath(), repo.getAbsolutePath(), repoName));
	}
	
	private void push(File repo) {
		runner.runCommand("push --directory=" + repo.getAbsolutePath());
	}
	
	private void cleanPush(File repo) {
		runner.runCommand("push --directory=" + repo.getAbsolutePath() + " --clean");
	}
	
	private void pull(File repo) {
		runner.runCommand("pull --directory=" + repo.getAbsolutePath());
	}
	
	private void cleanPull(File repo) {
		runner.runCommand("pull --directory=" + repo.getAbsolutePath() + " --clean");
	}

	private Collection<File> fillRepository(File repo) throws IOException {
		newSmallFileIn(repo);
		newMediumFileIn(repo);
		
		File dir1 = newDirIn(repo);
		newSmallFileIn(dir1);
		newMediumFileIn(dir1);
		
		File dir2 = newDirIn(repo);
		newSmallFileIn(dir2);
		newMediumFileIn(dir2);
		
		File dir21 = newDirIn(dir2);
		newSmallFileIn(dir21);
		newMediumFileIn(dir21);
		
		return getAllFiles(repo);
	}

	private File newMediumFileIn(File repo) throws IOException {
		File file = new File(repo, randString() + ".file");
		FileGenerator.createMediumFile(file);
		return file;
	}
	
	private File newSmallFileIn(File repo) throws IOException {
		File file = new File(repo, randString() + ".file");
		FileGenerator.createSmallFile(file);
		return file;
	}
	
	private File newDirIn(File repo) throws IOException {
		File file = new File(repo, "dir_" + randString());
		file.mkdir();
		return file;
	}
	
	private String fileId(File file, File repoDir) {
		return repoOperationsHelper.fileId(file, repoDir, this.repoName);
	}
	
}
