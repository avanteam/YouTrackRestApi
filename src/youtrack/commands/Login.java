package youtrack.commands;


import com.sun.istack.internal.NotNull;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import youtrack.YouTrack;
import youtrack.commands.base.RunningCommand;
import youtrack.exceptions.CommandExecutionException;

/**
 * Created by egor.malyshev on 31.03.2014.
 */
public class Login extends RunningCommand<YouTrack, String> {

    public Login(@NotNull YouTrack owner) {
        super(owner);
    }

    @Override
    public boolean usesAuthorization() {
        return false;
    }

    @Override
    public String getResult() {
        return method.getResponseHeader("Set-Cookie").getValue();
    }

    @Override
    public HttpMethodBase commandMethod(String baseHost) throws CommandExecutionException {
        final PostMethod postMethod = new PostMethod(baseHost + "user/login");
        postMethod.setRequestBody(new NameValuePair[]{new NameValuePair("login", getArguments().get("login")),
                        new NameValuePair("password", getArguments().get("password"))}
        );
        method = postMethod;
        return method;
    }
}