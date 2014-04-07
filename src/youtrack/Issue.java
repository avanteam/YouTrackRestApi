package youtrack;

import youtrack.commands.*;
import youtrack.commands.results.Result;
import youtrack.exceptions.NoSuchIssueFieldException;
import youtrack.exceptions.SetIssueFieldException;
import youtrack.issue.fields.IssueField;
import youtrack.issue.fields.values.BaseIssueFieldValue;
import youtrack.issue.fields.values.IssueFieldValue;
import youtrack.issue.fields.values.MultiUserFieldValue;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Egor.Malyshev on 19.12.13.
 * Provides access to a single issue and its fields.
 */
@XmlRootElement(name = "issue")
@XmlAccessorType(XmlAccessType.FIELD)
public class Issue {

	/*
	* These lists provide live access to issue comments and so on.
	*
	*/

	@XmlTransient
	public final CommandBasedList<IssueComment> comments;
	@XmlTransient
	public final CommandBasedList<IssueAttachment> attachments;
	@XmlTransient
	public final CommandBasedList<IssueLink> links;
	@XmlTransient
	public final CommandBasedList<IssueTag> tags;

	@XmlAttribute(name = "id")
	private String id;
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@XmlElement(name = "field")
	private List<IssueField> fieldArray;

	/*
	This is used to work around the issue with JAXB not being able to unmarshal a Map.
	 */
	@XmlTransient
	private HashMap<String, IssueField> fields;
	@XmlTransient
	private YouTrack youTrack;

	Issue() {
		comments = new CommandBasedList<IssueComment>(this, AddComment.class, RemoveComment.class, GetIssueComments.class);
		attachments = new CommandBasedList<IssueAttachment>(this, AddAttachment.class, RemoveAttachment.class, GetIssueAttachments.class);
		links = new CommandBasedList<IssueLink>(this, AddIssueLink.class, RemoveIssueLink.class, GetIssueLinks.class);
		tags = new CommandBasedList<IssueTag>(this, AddIssueTag.class, RemoveIssueTag.class, GetIssueTags.class);
	}

	public void setFieldByName(String fieldName, BaseIssueFieldValue value) throws SetIssueFieldException, IOException, NoSuchIssueFieldException {

		if (fields.containsKey(fieldName)) {

			Result result = youTrack.execute(new ModifyIssueField(this, fields.get(fieldName), value));

			if (result.success()) {

				throw new SetIssueFieldException(this, fields.get(fieldName), value);
			}

			fields.get(fieldName).setValue(value);
		} else throw new NoSuchIssueFieldException(this, fieldName);
	}

	public BaseIssueFieldValue getFieldByName(String fieldName) throws NoSuchIssueFieldException, IOException {

		if (fields.containsKey(fieldName)) {

			updateSelf();

			return fields.get(fieldName).getValue();

		} else throw new NoSuchIssueFieldException(this, fieldName);
	}

	@SuppressWarnings("UnusedDeclaration")
	protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		fields = new HashMap<String, IssueField>();
		for (IssueField issueField : fieldArray) {
			fields.put(issueField.getName(), issueField);
		}
	}

	@Override
	public String toString() {
		return "Issue{" +
				"id='" + id + '\'' +
				", fieldArray=" + fieldArray +
				'}';
	}

	public String getId() {
		return id;
	}

	public boolean isResolved() {
		return fields.containsKey("resolved");
	}

	public String getState() throws NoSuchIssueFieldException, IOException {
		return getFieldByName("State").getValue();
	}

	public void setState(String state) throws IOException, SetIssueFieldException, NoSuchIssueFieldException {
		setFieldByName("State", new IssueFieldValue(state));
	}

	public String getDescription() throws NoSuchIssueFieldException, IOException {
		return getFieldByName("description").getValue();
	}

	public void setDescription(String description) throws IOException, SetIssueFieldException, NoSuchIssueFieldException {

		Result result = youTrack.execute(new ModifyIssue(this, null, description));

		if (result.success()) {

			fields.get("description").setValue(new IssueFieldValue(description));

		} else
			throw new SetIssueFieldException(this, fields.get("description"), IssueFieldValue.createValue(description));

	}

	public String getSummary() throws IOException, NoSuchIssueFieldException {
		return getFieldByName("summary").getValue();
	}

	public void setSummary(String summary) throws IOException, SetIssueFieldException, NoSuchIssueFieldException {

		Result result = youTrack.execute(new ModifyIssue(this, summary, null));

		if (result.success()) {

			fields.get("summary").setValue(new IssueFieldValue(summary));

		} else throw new SetIssueFieldException(this, fields.get("description"), IssueFieldValue.createValue(summary));
	}

	public int getVotes() {
		try {
			return Integer.parseInt(getFieldByName("votes").getValue());
		} catch (Exception e) {
			return 0;
		}
	}

	public String getType() throws NoSuchIssueFieldException, IOException {
		return getFieldByName("Type").getValue();
	}

	public void setType(String type) throws IOException, SetIssueFieldException, NoSuchIssueFieldException {
		setFieldByName("Type", new IssueFieldValue(type));
	}

	public String getPriority() throws IOException, NoSuchIssueFieldException {
		return getFieldByName("Priority").getValue();
	}

	public void setPriority(String priority) throws IOException, SetIssueFieldException, NoSuchIssueFieldException {
		setFieldByName("Priority", new IssueFieldValue(priority));
	}

	public MultiUserFieldValue getAssignee() throws NoSuchIssueFieldException, IOException {
		return (MultiUserFieldValue) getFieldByName("Assignee");
	}

	public String getReporter() throws NoSuchIssueFieldException, IOException {
		return getFieldByName("reporterName").getValue();

	}

	public void setAssignee(String assignee, String fullName) throws IOException, SetIssueFieldException, NoSuchIssueFieldException {
		MultiUserFieldValue value = new MultiUserFieldValue(assignee);
		value.setFullName(fullName);
		setFieldByName("Assignee", value);
	}

	YouTrack getYouTrack() {
		return youTrack;
	}

	void setYouTrack(YouTrack youTrack) {
		this.youTrack = youTrack;
	}

	public void vote() throws IOException, NoSuchIssueFieldException {

		youTrack.execute(new ChangeIssueVotes(this, true)).success();
	}

	public void unVote() throws IOException, NoSuchIssueFieldException {

		youTrack.execute(new ChangeIssueVotes(this, false)).success();
	}

	private void updateSelf() throws IOException, NoSuchIssueFieldException {
		Issue issue = (Issue) youTrack.execute(new GetIssue(this.id)).getData();
		if (issue != null) {
			this.fields.clear();
			this.fields.putAll(issue.fields);
		}
	}
}