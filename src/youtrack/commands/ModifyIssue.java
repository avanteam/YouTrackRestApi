package youtrack.commands;


import com.sun.istack.internal.NotNull;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import youtrack.Issue;
import youtrack.commands.base.RunningCommand;
import youtrack.exceptions.CommandExecutionException;
import youtrack.exceptions.NoSuchIssueFieldException;

import java.io.IOException;

/**
 * Created by egor.malyshev on 02.04.2014.
 */
public class ModifyIssue extends RunningCommand<Issue, String> {

    public ModifyIssue(@NotNull Issue owner) {
        super(owner);
    }

    @Override
    public HttpMethodBase commandMethod(String baseHost) throws IOException, NoSuchIssueFieldException, CommandExecutionException {
        final PostMethod postMethod = new PostMethod(baseHost + "issue/" + owner.getId());
        final String summary = getArguments().get("summary");
        final String description = getArguments().get("description");
        postMethod.setRequestBody(new NameValuePair[]{
                new NameValuePair("summary", ((summary == null) ? owner.getSummary() : summary)),
                new NameValuePair("description", ((description == null) ? owner.getDescription() : description))
        });
        return postMethod;
    }
}