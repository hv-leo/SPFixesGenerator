import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

public class JRC {
  /**
   * Get JIRA issue by Key.
   * @param issueKey JIRA Issue Key
   * @return JIRA Issue
   * @throws Exception
  */
  public static Issue getIssue( String url, String user, String password, String issueKey ) throws Exception {
    URI jiraServerUri = new URI( url );
    JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    JiraRestClient client = factory.createWithBasicHttpAuthentication( jiraServerUri, user, password );
    Promise issuePromise = client.getIssueClient().getIssue( issueKey );
    return Optional.ofNullable( (Issue) issuePromise.claim() ).orElseThrow( ( ) -> new Exception( "No such issue" ) );
  }

  /**
   * Get Issues by fixVersion.
   * @param fixVersion
   * @return Issues that belongs to the fixVersion.
   * @throws URISyntaxException
  */
  public static Iterable<Issue> getIssuesByFixedVersion( String url, String user, String password, String fixVersion ) throws URISyntaxException {
    URI jiraServerUri = new URI( url );
    JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    JiraRestClient client = factory.createWithBasicHttpAuthentication( jiraServerUri, user, password );
    SearchRestClient searchClient = client.getSearchClient();

    SearchResult result = searchClient.searchJql( "fixVersion = '" + fixVersion + "'" ).claim();
    return result.getIssues();
  }
}
