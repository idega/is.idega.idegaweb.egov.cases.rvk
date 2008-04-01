package is.idega.idegaweb.egov.cases.rvk.data;


import is.idega.idegaweb.egov.cases.data.GeneralCase;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import com.idega.data.IDOEntity;
import com.idega.data.IDOFactory;

public class AnonymousInfoHomeImpl extends IDOFactory implements
		AnonymousInfoHome {
	public Class getEntityInterfaceClass() {
		return AnonymousInfo.class;
	}

	public AnonymousInfo create() throws CreateException {
		return (AnonymousInfo) super.createIDO();
	}

	public AnonymousInfo findByPrimaryKey(Object pk) throws FinderException {
		return (AnonymousInfo) super.findByPrimaryKeyIDO(pk);
	}

	public AnonymousInfo findByGeneralCase(GeneralCase genCase)
			throws FinderException {
		IDOEntity entity = this.idoCheckOutPooledEntity();
		Object pk = ((AnonymousInfoBMPBean) entity)
				.ejbFindByGeneralCase(genCase);
		this.idoCheckInPooledEntity(entity);
		return this.findByPrimaryKey(pk);
	}
}