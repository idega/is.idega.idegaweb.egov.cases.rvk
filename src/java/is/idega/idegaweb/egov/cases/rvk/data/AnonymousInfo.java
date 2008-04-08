package is.idega.idegaweb.egov.cases.rvk.data;


import is.idega.idegaweb.egov.cases.data.GeneralCase;
import com.idega.user.data.User;
import com.idega.data.IDOEntity;

public interface AnonymousInfo extends IDOEntity {
	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#getGeneralCase
	 */
	public GeneralCase getGeneralCase();

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#getUser
	 */
	public User getUser();

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#getPersonalID
	 */
	public String getPersonalID();

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#getEmail
	 */
	public String getEmail();

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#getPhone
	 */
	public String getPhone();

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#getIPAddress
	 */
	public String getIPAddress();

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#setGeneralCase
	 */
	public void setGeneralCase(GeneralCase genCase);

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#setUser
	 */
	public void setUser(User user);

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#setPersonalID
	 */
	public void setPersonalID(String personalID);

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#setEmail
	 */
	public void setEmail(String email);

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#setPhone
	 */
	public void setPhone(String phone);

	/**
	 * @see is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoBMPBean#setIPAddress
	 */
	public void setIPAddress(String IPAddress);
}