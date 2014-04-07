package youtrack.issue.fields;

import youtrack.issue.fields.values.BaseIssueFieldValue;
import youtrack.issue.fields.values.MultiUserFieldValue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by egor.malyshev on 28.03.2014.
 */
@XmlRootElement
public class MultiUserField extends IssueField {
	@XmlElement(name = "value")
	private MultiUserFieldValue value;

	public MultiUserField() {
	}

	@Override
	public MultiUserFieldValue getValue() {
		return this.value;
	}

	@Override
	public void setValue(BaseIssueFieldValue value) {
		this.value = (MultiUserFieldValue) value;
	}

}