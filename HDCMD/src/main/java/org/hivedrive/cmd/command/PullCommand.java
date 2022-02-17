package org.hivedrive.cmd.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hivedrive.cmd.model.PartInfo;
import org.hivedrive.cmd.service.ConnectionService;
import org.hivedrive.cmd.service.FileCompresssingService;
import org.hivedrive.cmd.service.FileSplittingService;
import org.hivedrive.cmd.service.RepositoryConfigService;
import org.hivedrive.cmd.service.SignatureService;
import org.hivedrive.cmd.service.SymetricEncryptionService;
import org.hivedrive.cmd.service.UserKeysService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Lazy
@Component("pull")
@Command(name = "pull", mixinStandardHelpOptions = true, version = "0.1", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PullCommand implements Runnable {

	@Option(names = { "-dir", "--directory" }, description = "")
	private File repositoryDirectory = new File(System.getProperty("user.dir"));
	
	private Logger logger = LoggerFactory.getLogger(PullCommand.class);

	private File workDirectory;
	
	@Autowired
	public PullCommand(ConnectionService connectionService,
			SymetricEncryptionService encryptionService, FileSplittingService fileSplittingService,
			FileCompresssingService fileComporessingService, SignatureService signatureService,
			UserKeysService userKeysService, RepositoryConfigService repositoryConfigService) {
		super();
		this.connectionService = connectionService;
		this.encryptionService = encryptionService;
		this.fileSplittingService = fileSplittingService;
		this.fileComporessingService = fileComporessingService;
		this.signatureService = signatureService;
		this.userKeysService = userKeysService;
		this.repositoryConfigService = repositoryConfigService;
	}

	private ConnectionService connectionService;
	private SymetricEncryptionService encryptionService;
	private FileSplittingService fileSplittingService;
	private FileCompresssingService fileComporessingService;
	private SignatureService signatureService;
	private UserKeysService userKeysService;
	private RepositoryConfigService repositoryConfigService;

	
	@Override
	public void run() {
		try {
			repositoryConfigService.setRepositoryDirectory(repositoryDirectory);
			
			userKeysService.loadKeys();
			
			workDirectory = new File(repositoryConfigService.getRepositoryDirectory(), ".temp");
			workDirectory.mkdir();
			List<PartInfo> parts = downloadParts();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				FileUtils.forceDelete(workDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}


	private List<PartInfo> downloadParts() {
		List<PartInfo> parts = connectionService.downloadParts();
		return parts;
	}
	
	
}
