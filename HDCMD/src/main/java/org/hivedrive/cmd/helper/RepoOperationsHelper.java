package org.hivedrive.cmd.helper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.hivedrive.cmd.model.TempFile;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class RepoOperationsHelper {

	@Autowired
	private RepositoryConfigService repositoryConfigService;
	
	public static Collection<File> getAllFiles(File repositoryDirectory) {
		IOFileFilter filter = new IOFileFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
			@Override
			public boolean accept(File file) {
//				List<String> excludedNames = Arrays.asList(".hivedrive", ".temp");
//				return !(excludedNames.contains(file.getName()));
				return !file.getName().startsWith(".");
			}
		};
		Collection<File> allFiles = FileUtils.listFiles(repositoryDirectory, filter,
				filter);
		return allFiles;
	}
	
	private static String fileHash(File file) {
		String fileHash = null;
		try {
			fileHash = DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileHash;
	}
	
	public String fileId(TempFile file) {
		return fileId(file.getOriginFile());
	}
	
	public String fileId(File file) {
		String fileId = repositoryConfigService.getConfig().getRepositoryName() + "-"
				+ filePath(file) + "-" + fileHash(file);
		return DigestUtils.md5DigestAsHex(fileId.getBytes());
	}
	
	public String filePath(File file) {
		return filePath(file, repositoryConfigService.getRepositoryDirectory());
	}

	public static String fileId(File file, File repoDir, String repoName) {
		String fileId = repoName + "-"
				+ filePath(file, repoDir) + "-" + fileHash(file);
		return DigestUtils.md5DigestAsHex(fileId.getBytes());
	}

	private static String filePath(File file, File repoDir) {
		return repoDir.toURI().relativize(file.toURI()).getPath();
	}
	
}
