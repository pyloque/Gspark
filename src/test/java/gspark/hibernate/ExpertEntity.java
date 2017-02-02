package gspark.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "expert")
public class ExpertEntity {
	@Id
	@Column(name = "user_name")
	private String userName;
	@Column(name = "real_name")
	private String realName;
	@Column(name = "password_hash")
	private String passwordHash;
	@Column(name = "create_ts")
	private long createTs;
	@Column(name = "update_ts")
	private long updateTs;

	public ExpertEntity() {
	}

	public ExpertEntity(String userName, String realName, String passwordHash, long createTs, long updateTs) {
		this.userName = userName;
		this.realName = realName;
		this.passwordHash = passwordHash;
		this.createTs = createTs;
		this.updateTs = updateTs;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(long createTs) {
		this.createTs = createTs;
	}

	public long getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(long updateTs) {
		this.updateTs = updateTs;
	}

}
