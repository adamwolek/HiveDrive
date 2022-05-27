package org.hivedrive.server.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class NodeEntity {

	public static String NEW_STATUS = "new";
	
	@Id
	@GeneratedValue
	private Long id;

//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, columnDefinition = "varchar(4096)")
	private String publicKey;

	private String status;

	private String address;

	private Long freeSpace;
	
	private Long usedSpace;
	
	@OneToMany(fetch = FetchType.EAGER)
	private List<ClientEntity> clients;
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public List<ClientEntity> getClients() {
		return clients;
	}

	public void setClients(List<ClientEntity> clients) {
		this.clients = clients;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	public Long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(Long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public Long getUsedSpace() {
		return usedSpace;
	}

	public void setUsedSpace(Long usedSpace) {
		this.usedSpace = usedSpace;
	}
	
	

}
