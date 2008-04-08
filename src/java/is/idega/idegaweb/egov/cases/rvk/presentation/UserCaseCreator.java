package is.idega.idegaweb.egov.cases.rvk.presentation;

import is.idega.idegaweb.egov.cases.data.CaseCategory;
import is.idega.idegaweb.egov.cases.data.CaseType;
import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.presentation.CaseCreator;
import is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfo;
import is.idega.idegaweb.egov.cases.rvk.data.AnonymousInfoHome;
import is.idega.idegaweb.egov.message.business.MessageSession;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.io.UploadFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.data.User;
import com.idega.util.FileUtil;

public class UserCaseCreator extends CaseCreator {

	protected static final String PARAMETER_TITLE = "prm_title";

	protected static final String PARAMETER_NAME = "prm_name";

	protected static final String PARAMETER_PERSONAL_ID = "prm_personal_id";

	protected static final String PARAMETER_EMAIL = "prm_email";

	protected static final String PARAMETER_EMAIL_CONF = "prm_email_conf";

	protected static final String PARAMETER_PHONE = "prm_phone";

	protected static final String PARAMETER_WANT_ANSWER = "prm_want_answer";

	protected static final String PARAMETER_ANSWER_TYPE_PHONE = "prm_answer_type_phone";

	protected static final String PARAMETER_ANSWER_TYPE_EMAIL = "prm_answer_type_email";

	//protected static final String PARAMETER_ANSWER_TYPE_WEB = "prm_answer_type_web";

	protected static final String PARAMETER_PRIORITY = "prm_priority";

	protected String defaultType = null;

	public String getBundleIdentifier() {
		return "is.idega.idegaweb.egov.cases.rvk";
	}

	protected void showPhaseOne(IWContext iwc) throws RemoteException {
		User user = getUser(iwc);
		Locale locale = iwc.getCurrentLocale();
		boolean hideOtherCategories = "true".equalsIgnoreCase(iwc
				.getParameter(PARAMETER_HIDE_OTHERS));

		CaseCategory category = null;
		if (iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK)) {
			try {
				category = getCasesBusiness(iwc).getCaseCategory(
						iwc.getParameter(PARAMETER_CASE_CATEGORY_PK));
			} catch (FinderException fe) {
				fe.printStackTrace();
			}
		}

		CaseCategory subCategory = null;
		if (getCasesBusiness(iwc).useSubCategories()
				&& iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY_PK)) {
			try {
				subCategory = getCasesBusiness(iwc).getCaseCategory(
						iwc.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK));
			} catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		Form form = new Form();
		form.setStyleClass("casesForm");
		form.add(new HiddenInput(PARAMETER_ACTION, String
				.valueOf(ACTION_PHASE_1)));

		addErrors(iwc, form);

		String headingText = this.iwrb.getLocalizedString(getPrefix()
				+ (this.iUseAnonymous ? "anonymous_application.case_creator"
						: "application.case_creator"), "Case creator");
		if (category != null) {
			headingText += " - " + category.getLocalizedCategoryName(locale);
		}

		Heading1 heading = new Heading1(headingText);
		heading.setStyleClass("applicationHeading");
		form.add(heading);

		form.add(getPhasesHeader(this.iwrb.getLocalizedString(getPrefix()
				+ "application.enter_new_case", "Enter new case"), 1, 2));

		form.add(getPersonInfo(iwc, user, true, true));

		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix()
				+ "case_creator.user_info", "User information"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		SelectorUtility util = new SelectorUtility();
		DropdownMenu categories = new DropdownMenu(PARAMETER_CASE_CATEGORY_PK);
		categories.keepStatusOnAction(true);
		categories.setStyleClass("caseCategoryDropdown");

		if (category != null && hideOtherCategories) {
			form.add(new HiddenInput(PARAMETER_HIDE_OTHERS, "true"));
			categories.addMenuElement(category.getPrimaryKey().toString(),
					category.getLocalizedCategoryName(locale));
			categories.setSelectedElement(category.getPrimaryKey().toString());
		} else {
			categories.addMenuElementFirst("", this.iwrb.getLocalizedString(
					"case_creator.select_category", "Select category"));
			Collection parentCategories = getCasesBusiness(iwc)
					.getCaseCategories();
			Iterator iter = parentCategories.iterator();
			while (iter.hasNext()) {
				CaseCategory element = (CaseCategory) iter.next();

				boolean addCategory = false;
				if (iCategories != null) {
					Iterator iterator = iCategories.iterator();
					while (iterator.hasNext()) {
						String categoryPK = (String) iterator.next();
						if (element.getPrimaryKey().toString().equals(
								categoryPK)) {
							addCategory = true;
						}
					}
				} else {
					addCategory = true;
				}

				if (addCategory) {
					String primaryKey = element.getPrimaryKey().toString();
					categories.addMenuElement(primaryKey, element
							.getLocalizedCategoryName(locale));
					if (category != null
							&& category.getPrimaryKey().equals(primaryKey)) {
						categories.setSelectedElement(primaryKey);
					}
				}
			}
		}
		//categories.setToSubmit();

		DropdownMenu subCategories = new DropdownMenu(
				PARAMETER_SUB_CASE_CATEGORY_PK);
		boolean addEmptyElement = true;
		if (category != null) {
			Collection subCats = getCasesBusiness(iwc).getSubCategories(
					category);
			if (!subCats.isEmpty()) {
				Iterator iter = subCats.iterator();
				while (iter.hasNext()) {
					CaseCategory subCat = (CaseCategory) iter.next();
					subCategories.addMenuElement(subCat.getPrimaryKey()
							.toString(), subCat
							.getLocalizedCategoryName(locale));
				}
			} else {
				addEmptyElement = false;
				subCategories.addMenuElement(category.getPrimaryKey()
						.toString(), iwrb.getLocalizedString(
						"case_creator.no_sub_category", "no sub category"));
			}
		}
		if (addEmptyElement) {
			subCategories.addMenuElementFirst("", this.iwrb.getLocalizedString(
					"case_creator.select_sub_category", "Select sub category"));
		}
		subCategories.keepStatusOnAction(true);
		//subCategories.setToSubmit();
		subCategories.setStyleClass("subCaseCategoryDropdown");

		DropdownMenu types = (DropdownMenu) util.getSelectorFromIDOEntities(
				new DropdownMenu(PARAMETER_CASE_TYPE_PK), getCasesBusiness(iwc)
						.getCaseTypes(), "getName");
		types.addMenuElementFirst("", this.iwrb.getLocalizedString(
				"case_creator.select_type", "Select type"));
		types.keepStatusOnAction(true);
		types.setStyleClass("caseTypeDropdown");

		CaseType firstType = getCasesBusiness(iwc).getFirstAvailableCaseType();
		HiddenInput hiddenType = null;
		if (this.defaultType == null) {
			hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK,
					firstType != null ? firstType.getPrimaryKey().toString()
							: "");
		} else {
			hiddenType = new HiddenInput(PARAMETER_CASE_TYPE_PK, defaultType);
		}

		TextInput title = new TextInput(PARAMETER_TITLE);
		title.keepStatusOnAction(true);

		TextArea message = new TextArea(PARAMETER_MESSAGE);
		message.setStyleClass("textarea");
		// message.keepStatusOnAction(true);
		String messageText = getMessageParameterValue(iwc);
		if (messageText != null) {
			message.setContent(messageText);
		}

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		String helperText = this.iwrb.getLocalizedString(getPrefix()
				+ "case_creator.information_text", "Information text here...");
		// If the category has a description use it, subcategories override!
		String tempHelperText = null; // so we don't make useless calls for
		// localized texts from the db!
		if (subCategory != null
				&& (tempHelperText = subCategory
						.getLocalizedCategoryDescription(locale)) != null
				&& !"".equals(tempHelperText)) {
			helperText = tempHelperText;
		} else if (category != null
				&& (tempHelperText = category
						.getLocalizedCategoryDescription(locale)) != null
				&& !"".equals(tempHelperText)) {
			helperText = tempHelperText;
		}

		helpLayer.add(new Text(helperText));
		section.add(helpLayer);

		if (this.iUseAnonymous) {
			Layer helpLayerExtra = new Layer(Layer.DIV);
			helpLayerExtra.setStyleClass("helperTextExtra");
			helpLayerExtra
					.add(new Text(
							this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "case_creator.information_text_extra",
											"Please note that we can only answer notifications from registered users due to the fact that anonymous notifications do not include any information about the sender.")));
			helpLayer.add(helpLayerExtra);
		}

		if (this.iUseAnonymous) {
			TextInput name = new TextInput(PARAMETER_NAME);
			TextInput personalID = new TextInput(PARAMETER_PERSONAL_ID);
			TextInput email = new TextInput(PARAMETER_EMAIL);
			TextInput emailConf = new TextInput(PARAMETER_EMAIL_CONF);
			TextInput phone = new TextInput(PARAMETER_PHONE);

			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label(new Span(new Text(this.iwrb
					.getLocalizedString("user_name", "Name"))), name);
			formItem.add(label);
			formItem.add(name);
			section.add(formItem);
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"user_personal_id", "Personal ID"))), personalID);
			formItem.add(label);
			formItem.add(personalID);
			section.add(formItem);
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"user_email", "Email"))), email);
			formItem.add(label);
			formItem.add(email);
			section.add(formItem);
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"user_email_confirm", "Confirm email"))), emailConf);
			formItem.add(label);
			formItem.add(emailConf);
			section.add(formItem);
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"user_phone", "Phone"))), phone);
			formItem.add(label);
			formItem.add(phone);
			section.add(formItem);
		} else {
			Phone userPhone = null;
			Email userEmail = null;
			if (user != null) {
				try {
					userPhone = getUserBusiness(iwc).getUsersHomePhone(user);
				} catch (NoPhoneFoundException e) {
				}
				try {
					userEmail = getUserBusiness(iwc).getUsersMainEmail(user);
				} catch (NoEmailFoundException e) {
				}
			}

			TextInput email = new TextInput(PARAMETER_EMAIL);
			TextInput phone = new TextInput(PARAMETER_PHONE);

			if (userEmail != null) {
				email.setValue(userEmail.getEmailAddress());
			}
			
			if (userPhone != null) {
				phone.setValue(userPhone.getNumber());
			}
			
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label(new Span(new Text(this.iwrb
					.getLocalizedString("user_email", "Email"))), email);
			formItem.add(label);
			formItem.add(email);
			section.add(formItem);
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"user_phone", "Phone"))), phone);
			formItem.add(label);
			formItem.add(phone);
			section.add(formItem);
		}

		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix()
				+ "case_creator.enter_case", "New case"));
		heading.setStyleClass("subHeader");
		form.add(heading);
		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		if (getCasesBusiness(iwc).useTypes() && this.defaultType == null) {
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_CASE_TYPE_PK)) {
				formItem.setStyleClass("hasError");
			}
			Label label = new Label(new Span(new Text(this.iwrb
					.getLocalizedString("case_type", "Case type"))), types);
			formItem.add(label);
			formItem.add(types);
			section.add(formItem);
		} else {
			form.add(hiddenType);
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		if (hasError(PARAMETER_CASE_CATEGORY_PK)) {
			formItem.setStyleClass("hasError");
		}
		Label label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
				"case_category", "Case category"))), categories);
		formItem.add(label);
		formItem.add(categories);
		section.add(formItem);

		if (getCasesBusiness(iwc).useSubCategories()) {
			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("required");
			if (hasError(PARAMETER_SUB_CASE_CATEGORY_PK)) {
				formItem.setStyleClass("hasError");
			}
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"sub_case_category", "Sub case category"))), subCategories);
			formItem.add(label);
			formItem.add(subCategories);
			section.add(formItem);
		}

		if (getCasesBusiness(iwc).allowAttachments()) {
			FileInput file = new FileInput();

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setID("attachment");
			label = new Label(this.iwrb.getLocalizedString("attachment",
					"Attachment"), file);
			formItem.add(label);
			formItem.add(file);
			section.add(formItem);
		}

		if (this.iUseSessionUser) {
			DropdownMenu priority = new DropdownMenu(PARAMETER_PRIORITY);
			priority.addMenuElementFirst("-1", this.iwrb.getLocalizedString(
					"case_creator.select_priority", "Select priority"));
			priority.keepStatusOnAction(true);
			priority.setStyleClass("priorityTypeDropdown");
			priority.addMenuElement("0", this.iwrb.getLocalizedString(
					"case_creator.priority_low", "Low"));
			priority.addMenuElement("1", this.iwrb.getLocalizedString(
					"case_creator.priority_medium", "Medium"));
			priority.addMenuElement("2", this.iwrb.getLocalizedString(
					"case_creator.priority_high", "High"));
			priority.addMenuElement("3", this.iwrb.getLocalizedString(
					"case_creator.priority_urget", "Urgent"));

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					"case_creator.priority", "Priority"))), priority);
			formItem.add(label);
			formItem.add(priority);
			section.add(formItem);
		}

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		if (hasError(PARAMETER_REGARDING)) {
			formItem.setStyleClass("hasError");
		}
		label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
				getPrefix() + "title", "Title"))), title);
		formItem.add(label);
		formItem.add(title);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		if (hasError(PARAMETER_MESSAGE)) {
			formItem.setStyleClass("hasError");
		}
		label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
				getPrefix() + "message", "Message"))), message);
		formItem.add(label);
		formItem.add(message);
		section.add(formItem);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix()
				+ "case_creator.want_answer", "Want answer"));
		heading.setStyleClass("subHeader");
		form.add(heading);
		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		if (this.iUseAnonymous) {
			CheckBox wantAnswer = new CheckBox(PARAMETER_WANT_ANSWER,
					Boolean.FALSE.toString());
			wantAnswer.setStyleClass("checkbox");
			wantAnswer.keepStatusOnAction(true);

			Paragraph paragraph = new Paragraph();
			paragraph.setStyleClass("wantAnswerText");
			paragraph
					.add(new Text(
							this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "case_creator.want_anwer_text",
											"If you would like an answer to your case please check the checkbox here below.")));
			section.add(paragraph);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_anonymous",
					"Want answer"))), wantAnswer);
			formItem.add(wantAnswer);
			formItem.add(label);
			section.add(formItem);

			paragraph = new Paragraph();
			paragraph.setStyleClass("answerTypeText");
			paragraph
					.add(new Text(
							this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "case_creator.answer_type_text",
											"If you wanted an answer to your case please select the answer type here below.")));
			section.add(paragraph);

			CheckBox wantAnswerEmail = new CheckBox(
					PARAMETER_ANSWER_TYPE_EMAIL, Boolean.FALSE.toString());
			wantAnswerEmail.setStyleClass("checkbox");
			wantAnswerEmail.keepStatusOnAction(true);

			CheckBox wantAnswerPhone = new CheckBox(
					PARAMETER_ANSWER_TYPE_PHONE, Boolean.FALSE.toString());
			wantAnswerPhone.setStyleClass("checkbox");
			wantAnswerPhone.keepStatusOnAction(true);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_email",
					"Email answer"))), wantAnswerEmail);
			formItem.add(wantAnswerEmail);
			formItem.add(label);
			section.add(formItem);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_phone",
					"Phone answer"))), wantAnswerPhone);
			formItem.add(wantAnswerPhone);
			formItem.add(label);
			section.add(formItem);
		} else if (!iUseSessionUser) {
			MessageSession messageSession = getMessageSession(iwc);

			CheckBox wantAnswer = new CheckBox(PARAMETER_WANT_ANSWER);
			wantAnswer.setChecked(!messageSession.getIfUserPreferesMessageByEmail());
			
			wantAnswer.setStyleClass("checkbox");
			wantAnswer.keepStatusOnAction(true);

			Paragraph paragraph = new Paragraph();
			paragraph.setStyleClass("wantAnswerText");
			paragraph
					.add(new Text(
							this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "case_creator.email_anwer_logged_in_info",
											"If you would like an answer to your case please check the checkbox here below.")));
			section.add(paragraph);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_logged_in",
					"Want answer"))), wantAnswer);
			formItem.add(wantAnswer);
			formItem.add(label);
			section.add(formItem);
		} else {
			MessageSession messageSession = getMessageSession(iwc);

			CheckBox wantAnswer = new CheckBox(PARAMETER_WANT_ANSWER);
			wantAnswer.setChecked(messageSession.getIfUserPreferesMessageByEmail());

			wantAnswer.setStyleClass("checkbox");
			wantAnswer.keepStatusOnAction(true);

			Paragraph paragraph = new Paragraph();
			paragraph.setStyleClass("wantAnswerText");
			paragraph
					.add(new Text(
							this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "case_creator.email_anwer_admin_info",
											"If you would like an answer to your case please check the checkbox here below.")));
			section.add(paragraph);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_admin",
					"Want answer"))), wantAnswer);
			formItem.add(wantAnswer);
			formItem.add(label);
			section.add(formItem);

			paragraph = new Paragraph();
			paragraph.setStyleClass("answerTypeText");
			paragraph.add(new Text(this.iwrb.getLocalizedString(getPrefix()
					+ "case_creator.email_anwer_type_admin_info",
					"If yes, then how")));
			section.add(paragraph);

			CheckBox wantAnswerEmail = new CheckBox(
					PARAMETER_ANSWER_TYPE_EMAIL, Boolean.FALSE.toString());
			wantAnswerEmail.setStyleClass("checkbox");
			wantAnswerEmail.keepStatusOnAction(true);

			CheckBox wantAnswerPhone = new CheckBox(
					PARAMETER_ANSWER_TYPE_PHONE, Boolean.FALSE.toString());
			wantAnswerPhone.setStyleClass("checkbox");
			wantAnswerPhone.keepStatusOnAction(true);


			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_email_admin",
					"Email answer"))), wantAnswerEmail);
			formItem.add(wantAnswerEmail);
			formItem.add(label);
			section.add(formItem);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.want_answer_phone_admin",
					"Phone answer"))), wantAnswerPhone);
			formItem.add(wantAnswerPhone);
			formItem.add(label);
			section.add(formItem);
		}

		if (getCasesBusiness(iwc).allowPrivateCases() && !iUseAnonymous
				&& iUseSessionUser) {
			heading = new Heading1(this.iwrb.getLocalizedString(getPrefix()
					+ "case_creator.private_case_info",
					"Private case information"));
			heading.setStyleClass("subHeader");
			form.add(heading);

			section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);

			CheckBox isPrivate = new CheckBox(PARAMETER_PRIVATE, Boolean.TRUE
					.toString());
			isPrivate.setStyleClass("checkbox");
			isPrivate.keepStatusOnAction(true);

			Paragraph paragraph = new Paragraph();
			paragraph.setStyleClass("privateText");
			paragraph
					.add(new Text(
							this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "case_creator.private_text",
											"If you would like your case to be handled confidentially please check the checkbox here below.")));
			section.add(paragraph);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.request_private_handling",
					"I request for my case to be handled confidentially"))),
					isPrivate);
			formItem.add(isPrivate);
			formItem.add(label);
			section.add(formItem);

			section.add(clear);
		}

		if (iUseAnonymous) {
			heading = new Heading1(this.iwrb.getLocalizedString(getPrefix()
					+ "case_creator.Terms", "Terms"));
			heading.setStyleClass("subHeader");
			form.add(heading);
			
			section = new Layer(Layer.DIV);
			section.setStyleClass("formSection");
			form.add(section);
			Paragraph paragraph = new Paragraph();
			paragraph.setStyleClass("termText");
			paragraph
					.add(new Text(this.iwrb.getLocalizedString(getPrefix()
							+ "case_creator.term_text",
							"Legal terms... bla bla bla bla.")));
			section.add(paragraph);

			CheckBox acceptsTerms = new CheckBox(
					PARAMETER_ANSWER_TYPE_PHONE, Boolean.FALSE.toString());
			acceptsTerms.setStyleClass("checkbox");
			acceptsTerms.keepStatusOnAction(true);


			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			formItem.setStyleClass("radioButtonItem");
			formItem.setStyleClass("required");
			label = new Label(new Span(new Text(this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.accepts_terms",
					"accepts_terms"))), acceptsTerms);
			formItem.add(acceptsTerms);
			formItem.add(label);
			section.add(formItem);
		}
		
		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link next = getButtonLink(this.iwrb.getLocalizedString("send", "Send"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void showOverview(IWContext iwc) throws RemoteException {
		Locale locale = iwc.getCurrentLocale();

		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc
				.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		// String regarding = iwc.getParameter(PARAMETER_REGARDING);
		String title = iwc.getParameter(PARAMETER_TITLE);
		String message = getMessageParameterValue(iwc);

		String name = iwc.getParameter(PARAMETER_NAME);
		String personalID = iwc.getParameter(PARAMETER_PERSONAL_ID);
		String email = iwc.getParameter(PARAMETER_EMAIL);
		String emailConf = iwc.getParameter(PARAMETER_EMAIL_CONF);
		String phone = iwc.getParameter(PARAMETER_PHONE);

		String wantAnswer = iwc.getParameter(PARAMETER_WANT_ANSWER);
		// String answerType = iwc.getParameter(PARAMETER_ANSWER_TYPE);

		ICFile attachment = null;
		UploadFile uploadFile = iwc.getUploadedFile();
		if (uploadFile != null && uploadFile.getName() != null
				&& uploadFile.getName().length() > 0) {
			try {
				FileInputStream input = new FileInputStream(uploadFile
						.getRealPath());

				attachment = ((ICFileHome) IDOLookup.getHome(ICFile.class))
						.create();
				attachment.setName(uploadFile.getName());
				attachment.setMimeType(uploadFile.getMimeType());
				attachment.setFileValue(input);
				attachment.setFileSize((int) uploadFile.getSize());
				attachment.store();

				uploadFile.setId(((Integer) attachment.getPrimaryKey())
						.intValue());
				try {
					FileUtil.delete(uploadFile);
				} catch (Exception ex) {
					System.err
							.println("MediaBusiness: deleting the temporary file at "
									+ uploadFile.getRealPath() + " failed.");
				}
			} catch (RemoteException e) {
				e.printStackTrace(System.err);
				uploadFile.setId(-1);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CreateException ce) {
				ce.printStackTrace();
			}
		}

		CaseCategory category = null;
		if (caseCategoryPK != null && !"".equals(caseCategoryPK)) {
			try {
				category = getCasesBusiness(iwc)
						.getCaseCategory(caseCategoryPK);
			} catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		CaseCategory subCategory = null;
		if (getCasesBusiness(iwc).useSubCategories()
				&& subCaseCategoryPK != null && !"".equals(subCaseCategoryPK)) {
			try {
				subCategory = getCasesBusiness(iwc).getCaseCategory(
						subCaseCategoryPK);
			} catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		CaseType type = null;
		if (caseTypePK != null && !"".equals(caseTypePK)) {
			try {
				type = getCasesBusiness(iwc).getCaseType(caseTypePK);
			} catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		if (this.getCasesBusiness(iwc).useSubCategories()) {
			if (!iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY_PK)) {
				setError(PARAMETER_CASE_CATEGORY_PK, this.iwrb
						.getLocalizedString("case_creator.sub_category_empty",
								"You must select a category"));
			}
		}
		if (!iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK)) {
			setError(PARAMETER_CASE_CATEGORY_PK, this.iwrb
					.getLocalizedString("case_creator.category_empty",
							"You must select a category"));
		}
		if (!iwc.isParameterSet(PARAMETER_CASE_TYPE_PK)) {
			setError(PARAMETER_CASE_TYPE_PK, this.iwrb.getLocalizedString(
					"case_creator.type_empty", "You must select a type"));
		}
		if (!iwc.isParameterSet(PARAMETER_TITLE)) {
			setError(PARAMETER_TITLE, this.iwrb.getLocalizedString(getPrefix()
					+ "case_creator.title_empty",
					"You must enter a title for the case"));
		}
		if (!iwc.isParameterSet(PARAMETER_MESSAGE)) {
			setError(PARAMETER_MESSAGE, this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.message_empty",
					"You must enter a message"));
		}

		if (hasErrors()) {
			showPhaseOne(iwc);
			return;
		}

		User user = getUser(iwc);

		Form form = new Form();
		form.setStyleClass("casesForm");
		form.setStyleClass("overview");
		form.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_OVERVIEW));

		form.maintainParameter(PARAMETER_CASE_TYPE_PK);
		form.maintainParameter(PARAMETER_CASE_CATEGORY_PK);
		form.maintainParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		form.maintainParameter(PARAMETER_PRIVATE);
		form.maintainParameter(PARAMETER_REGARDING);
		if (attachment != null) {
			form.add(new HiddenInput(PARAMETER_ATTACHMENT_PK, attachment
					.getPrimaryKey().toString()));
		}

		String headingText = this.iwrb.getLocalizedString(getPrefix()
				+ (this.iUseAnonymous ? "anonymous_application.case_creator"
						: "application.case_creator"), "Case creator");
		if (category != null) {
			headingText += " - " + category.getLocalizedCategoryName(locale);
		}
		Heading1 heading = new Heading1(this.iwrb.getLocalizedString(
				getPrefix() + "application.case_creator", "Case creator"));
		heading.setStyleClass("applicationHeading");
		form.add(heading);

		form.add(getPhasesHeader(this.iwrb.getLocalizedString(getPrefix()
				+ "application.overview", "Overview"), 2, 3));

		form.add(getPersonInfo(iwc, user));

		heading = new Heading1(this.iwrb.getLocalizedString(getPrefix()
				+ "case_creator.enter_case_overview", "New case overview"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Span typeSpan = new Span(new Text(type.getName()));
		Span categorySpan = new Span(new Text(category
				.getLocalizedCategoryName(locale)));
		// Span regardingSpan = new Span(new Text(regarding));
		Span messageSpan = new Span(new Text(message));

		if (getCasesBusiness(iwc).useTypes()) {
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("case_type",
					"Case type"));
			formItem.add(label);
			formItem.add(typeSpan);
			section.add(formItem);
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label();
		label.setLabel(this.iwrb.getLocalizedString("case_category",
				"Case category"));
		formItem.add(label);
		formItem.add(categorySpan);
		section.add(formItem);

		if (getCasesBusiness(iwc).useSubCategories()
				&& !subCategory.equals(category)) {
			Layer subCategorySpan = new Layer(Layer.SPAN);
			subCategorySpan.add(new Text(subCategory
					.getLocalizedCategoryName(locale)));

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("sub_case_category",
					"Sub case category"));
			formItem.add(label);
			formItem.add(subCategorySpan);
			section.add(formItem);
		}

		if (attachment != null) {
			Link link = new Link(new Text(attachment.getName()));
			link.setFile(attachment);
			link.setTarget(Link.TARGET_BLANK_WINDOW);

			Layer attachmentSpan = new Layer(Layer.SPAN);
			attachmentSpan.add(link);

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label();
			label.setLabel(this.iwrb.getLocalizedString("attachment",
					"Attachment"));
			formItem.add(label);
			formItem.add(attachmentSpan);
			section.add(formItem);
		}

		/*
		 * formItem = new Layer(Layer.DIV); formItem.setStyleClass("formItem");
		 * label = new Label();
		 * label.setLabel(this.iwrb.getLocalizedString(getPrefix() +
		 * "regarding", "Regarding")); formItem.add(label);
		 * formItem.add(regardingSpan); section.add(formItem);
		 */

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("informationItem");
		label = new Label();
		label.setLabel(this.iwrb.getLocalizedString(getPrefix() + "message",
				"Message"));
		formItem.add(label);
		formItem.add(messageSpan);
		section.add(formItem);

		Layer clear = new Layer(Layer.DIV);
		clear.setStyleClass("Clear");
		section.add(clear);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(this.iwrb.getLocalizedString("previous",
				"Previous"));
		back.setStyleClass("homeButton");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_PHASE_1));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link next = getButtonLink(this.iwrb.getLocalizedString("send", "Send"));
		next.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		next.setToFormSubmit(form);
		bottom.add(next);

		add(form);
	}

	protected void save(IWContext iwc) throws RemoteException {
		if (this.getCasesBusiness(iwc).useSubCategories()) {
			if (!iwc.isParameterSet(PARAMETER_SUB_CASE_CATEGORY_PK)) {
				setError(PARAMETER_CASE_CATEGORY_PK, this.iwrb
						.getLocalizedString("case_creator.sub_category_empty",
								"You must select a category"));
			}
		}
		if (!iwc.isParameterSet(PARAMETER_CASE_CATEGORY_PK)) {
			setError(PARAMETER_CASE_CATEGORY_PK, this.iwrb
					.getLocalizedString("case_creator.category_empty",
							"You must select a category"));
		}
		if (!iwc.isParameterSet(PARAMETER_CASE_TYPE_PK)) {
			setError(PARAMETER_CASE_TYPE_PK, this.iwrb.getLocalizedString(
					"case_creator.type_empty", "You must select a type"));
		}
		if (!iwc.isParameterSet(PARAMETER_TITLE)) {
			setError(PARAMETER_TITLE, this.iwrb.getLocalizedString(getPrefix()
					+ "case_creator.title_empty",
					"You must enter a title for the case"));
		}
		if (!iwc.isParameterSet(PARAMETER_MESSAGE)) {
			setError(PARAMETER_MESSAGE, this.iwrb.getLocalizedString(
					getPrefix() + "case_creator.message_empty",
					"You must enter a message"));
		}

		if (hasErrors()) {
			showPhaseOne(iwc);
			return;
		}

		ICFile attachment = null;
		UploadFile uploadFile = iwc.getUploadedFile();
		if (uploadFile != null && uploadFile.getName() != null
				&& uploadFile.getName().length() > 0) {
			try {
				FileInputStream input = new FileInputStream(uploadFile
						.getRealPath());

				attachment = ((ICFileHome) IDOLookup.getHome(ICFile.class))
						.create();
				attachment.setName(uploadFile.getName());
				attachment.setMimeType(uploadFile.getMimeType());
				attachment.setFileValue(input);
				attachment.setFileSize((int) uploadFile.getSize());
				attachment.store();

				uploadFile.setId(((Integer) attachment.getPrimaryKey())
						.intValue());
				try {
					FileUtil.delete(uploadFile);
				} catch (Exception ex) {
					System.err
							.println("MediaBusiness: deleting the temporary file at "
									+ uploadFile.getRealPath() + " failed.");
				}
			} catch (RemoteException e) {
				e.printStackTrace(System.err);
				uploadFile.setId(-1);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CreateException ce) {
				ce.printStackTrace();
			}
		}

		String regarding = iwc.getParameter(PARAMETER_REGARDING);
		String message = getMessageParameterValue(iwc);
		iwc.removeSessionAttribute(PARAMETER_MESSAGE);

		Object caseCategoryPK = iwc.getParameter(PARAMETER_CASE_CATEGORY_PK);
		Object subCaseCategoryPK = iwc
				.getParameter(PARAMETER_SUB_CASE_CATEGORY_PK);
		Object caseTypePK = iwc.getParameter(PARAMETER_CASE_TYPE_PK);
		//Object attachmentPK = iwc.getParameter(PARAMETER_ATTACHMENT_PK);
		boolean isPrivate = iwc.isParameterSet(PARAMETER_PRIVATE);

		//new stuff
		String title = iwc.getParameter(PARAMETER_TITLE);
		String want_answer = iwc.getParameter(PARAMETER_WANT_ANSWER);
		String phone_answer = iwc.getParameter(PARAMETER_ANSWER_TYPE_PHONE);
		String email_answer = iwc.getParameter(PARAMETER_ANSWER_TYPE_EMAIL);
		String priority = iwc.getParameter(PARAMETER_PRIORITY);

		String name = iwc.getParameter(PARAMETER_NAME);
		String ssn = iwc.getParameter(PARAMETER_PERSONAL_ID);
		String email = iwc.getParameter(PARAMETER_EMAIL);
		String email_conf = iwc.getParameter(PARAMETER_EMAIL_CONF);
		String phone = iwc.getParameter(PARAMETER_PHONE);
		
		Locale locale = iwc.getCurrentLocale();

		CaseCategory category = null;
		if (caseCategoryPK != null && !"".equals(caseCategoryPK)) {
			try {
				category = getCasesBusiness(iwc)
						.getCaseCategory(caseCategoryPK);
			} catch (FinderException fe) {
				throw new IBORuntimeException(fe);
			}
		}

		try {
			User user = getUser(iwc);
			GeneralCase theCase = getCasesBusiness(iwc)
					.storeGeneralCase(
							user,
							getCasesBusiness(iwc).useSubCategories() ? subCaseCategoryPK
									: caseCategoryPK,
							caseTypePK,
							attachment.getPrimaryKey(),
							regarding,
							message,
							getType(),
							isPrivate,
							getCasesBusiness(iwc).getIWResourceBundleForUser(
									user, iwc, this.getBundle(iwc)));

			theCase.setTitle(title);
			theCase.setPriority(priority);
			theCase.setWantReply(want_answer);
			theCase.setWantReplyEmail(email_answer);
			theCase.setWantReplyPhone(phone_answer);
			if (iUseSessionUser) {
				theCase.setCreator(iwc.getCurrentUser());
			}
			theCase.store();
			
			AnonymousInfoHome anonInfoHome = getAnonymousInfoHome();
			AnonymousInfo info = anonInfoHome.create();
			info.setGeneralCase(theCase);
			info.setIPAddress(iwc.getRemoteIpAddress());
			info.setPersonalID(ssn);
			info.setPhone(phone);
			info.setEmail(email);
			info.setUser(user);
			info.store();
			
			String headingText = this.iwrb
					.getLocalizedString(
							getPrefix()
									+ (this.iUseAnonymous ? "anonymous_application.case_creator"
											: "application.case_creator"),
							"Case creator");
			if (category != null) {
				headingText += " - "
						+ category.getLocalizedCategoryName(locale);
			}
			Heading1 heading = new Heading1(this.iwrb.getLocalizedString(
					getPrefix() + "application.case_creator", "Case creator"));
			heading.setStyleClass("applicationHeading");
			add(heading);

			addPhasesReceipt(
					iwc,
					this.iwrb
							.getLocalizedString(getPrefix()
									+ "case_creator.save_completed",
									"Application sent"),
					this.iwrb
							.getLocalizedString(getPrefix()
									+ "case_creator.save_completed",
									"Application sent"),
					user != null ? this.iwrb
							.getLocalizedString(getPrefix()
									+ "case_creator.save_confirmation",
									"Your case has been sent and will be processed accordingly.")
							: this.iwrb
									.getLocalizedString(
											getPrefix()
													+ "anonymous_case_creator.save_confirmation",
											"Your case has been sent and will be processed accordingly."),
					2, 2);

			Layer clearLayer = new Layer(Layer.DIV);
			clearLayer.setStyleClass("Clear");
			add(clearLayer);

			Layer bottom = new Layer(Layer.DIV);
			bottom.setStyleClass("bottom");
			add(bottom);

			if (!this.iUseAnonymous && iwc.isLoggedOn()) {
				try {
					ICPage page = getUserBusiness(iwc).getHomePageForUser(
							iwc.getCurrentUser());
					Link link = getButtonLink(this.iwrb.getLocalizedString(
							"my_page", "My page"));
					link.setStyleClass("homeButton");
					link.setPage(page);
					bottom.add(link);
				} catch (FinderException fe) {
					fe.printStackTrace();
				}
			} else {
				Link link = getButtonLink(this.iwrb.getLocalizedString("close",
						"Close"));
				link.setStyleClass("homeButton");
				link.setAsCloseLink();
				bottom.add(link);
			}
		} catch (CreateException ce) {
			ce.printStackTrace();
			throw new IBORuntimeException(ce);
		}
	}

	public void setDefaultCaseType(String defaultCaseType) {
		this.defaultType = defaultCaseType;
	}

	public String getDefaultCaseType() {
		return this.defaultType;
	}
	
	private MessageSession getMessageSession(IWContext iwc) throws RemoteException {
		return (MessageSession) com.idega.business.IBOLookup.getSessionInstance(iwc, MessageSession.class);
	}

	private AnonymousInfoHome getAnonymousInfoHome() {
		try {
			return (AnonymousInfoHome) IDOLookup.getHome(AnonymousInfo.class);
		} catch (IDOLookupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}