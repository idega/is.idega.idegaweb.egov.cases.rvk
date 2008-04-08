package is.idega.idegaweb.egov.cases.rvk.data;

import javax.ejb.FinderException;

import is.idega.idegaweb.egov.cases.data.GeneralCase;

import com.idega.data.GenericEntity;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.User;

public class AnonymousInfoBMPBean extends GenericEntity implements
		AnonymousInfo {

	protected static String ENTITY_NAME = "comm_case_anon_info";
	
	protected static String COLUMN_CASE = "comm_case_id";
	protected static String COLUMN_USER = "ic_user_id";
	protected static String COLUMN_PERSONAL_ID = "personal_id";
	protected static String COLUMN_EMAIL = "email";
	protected static String COLUMN_PHONE = "phone";
	protected static String COLUMN_IP_ADDRESS = "ip_address";
	
	public String getEntityName() {
		return ENTITY_NAME;
	}

	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addOneToOneRelationship(COLUMN_CASE, GeneralCase.class);
		addManyToOneRelationship(COLUMN_USER, User.class);
		addAttribute(COLUMN_PERSONAL_ID, "Personal id", String.class);
		addAttribute(COLUMN_EMAIL, "Email", String.class);
		addAttribute(COLUMN_PHONE, "Phone", String.class);
		addAttribute(COLUMN_IP_ADDRESS, "IP address", String.class);
	}
	
	//getters
	public GeneralCase getGeneralCase() {
		return (GeneralCase) getColumnValue(COLUMN_CASE);
	}
	
	public User getUser() {
		return (User) getColumnValue(COLUMN_USER);
	}
	
	public String getPersonalID() {
		return getStringColumnValue(COLUMN_PERSONAL_ID);
	}
	
	public String getEmail() {
		return getStringColumnValue(COLUMN_EMAIL);
	}
	
	public String getPhone() {
		return getStringColumnValue(COLUMN_PHONE);
	}
	
	public String getIPAddress() {
		return getStringColumnValue(COLUMN_IP_ADDRESS);
	}
	
	//setters
	public void setGeneralCase(GeneralCase genCase) {
		setColumn(COLUMN_CASE, genCase);
	}
	
	public void setUser(User user) {
		setColumn(COLUMN_USER, user);
	}
	
	public void setPersonalID(String personalID) {
		setColumn(COLUMN_PERSONAL_ID, personalID);
	}
	
	public void setEmail(String email) {
		setColumn(COLUMN_EMAIL, email);		
	}
	
	public void setPhone(String phone) {
		setColumn(COLUMN_PHONE, phone);
	}
	
	public void setIPAddress(String IPAddress) {
		setColumn(COLUMN_IP_ADDRESS, IPAddress);
	}
	
	//ejb
	public Object ejbFindByGeneralCase(GeneralCase genCase) throws FinderException {
		Table table = new Table(this);
		
		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new MatchCriteria(table.getColumn(COLUMN_CASE), MatchCriteria.EQUALS, genCase));
		
		return idoFindOnePKByQuery(query);
	}
	
}