package is.idega.idegaweb.egov.cases.rvk.data;


import is.idega.idegaweb.egov.cases.data.GeneralCase;
import javax.ejb.CreateException;
import com.idega.data.IDOHome;
import javax.ejb.FinderException;

public interface AnonymousInfoHome extends IDOHome {
	public AnonymousInfo create() throws CreateException;

	public AnonymousInfo findByPrimaryKey(Object pk) throws FinderException;

	public AnonymousInfo findByGeneralCase(GeneralCase genCase)
			throws FinderException;
}