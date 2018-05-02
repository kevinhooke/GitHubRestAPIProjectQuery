package kh.githubapi.projectdata;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import kh.githubapi.projectdata.domain.GitHubRepository;
/**
 * Retrieve GitHub project names using GitHub API
 * 
 * GitHub API limit 5000 calls/hour, ~1.4/sec. If you do a call a second and sleep 1 sec, this will be within the limit.
 * @author kevinhooke
 *
 */
@SpringBootApplication
public class RetrieveProjectsApplication implements CommandLineRunner{

	@Value("${github.client.id}")
	private String githubClientId;
	
	@Value("${github.client.secret}")
	private String githubClientSecret;
	
	//https://api.github.com
	//GET /search/repositories
	//?client_id=xxxx&client_secret=yyyy'

	
	public static void main(String[] args) {
		SpringApplication.run(RetrieveProjectsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("App starting...");
		System.out.println("GitHub clientid: " + this.githubClientId);
		RestTemplate rest = new RestTemplate();
		
		//search api
//		ResponseEntity<String> result = rest.getForEntity("https://api.github.com/search/repositories?q=java&client_id="
//				+ this.githubClientId + "&client_secret="
//				+ this.githubClientSecret, 
//				String.class);
		
		//retrieve as String or debugging
		//		ResponseEntity<String> result = rest.getForEntity("https://api.github.com/repositories?client_id="
//				+ this.githubClientId + "&client_secret="
//				+ this.githubClientSecret, 
//				String.class);
		//System.out.println(result.getBody());
		
		ResponseEntity<GitHubRepository[]> result = rest.getForEntity("https://api.github.com/repositories?client_id="
				+ this.githubClientId + "&client_secret="
				+ this.githubClientSecret, 
				GitHubRepository[].class);
		
		System.out.println(result.getStatusCodeValue());

		//get Link header for next page URL
		//Link: <https://api.github.com/repositories?since=364>; rel="next"
		HttpHeaders headers = result.getHeaders();
		
		String nextPageLink = getNextPageLinkFromHeader(headers);
		
		List<String> limits = headers.get("X-RateLimit-Limit");
		System.out.println("X-RateLimit-Limit: " + limits.get(0));
		List<String> remaining = headers.get("X-RateLimit-Remaining");
		System.out.println("X-RateLimit-Remaining: " + remaining.get(0));
		List<String> limitReset = headers.get("X-RateLimit-Reset");
		System.out.println("X-RateLimit-Reset: " + limitReset.get(0));
		
		for(GitHubRepository repoDetail : result.getBody()) {
			System.out.println(repoDetail.getName());
			
			//TODO write to file
		}
	}

	private String getNextPageLinkFromHeader(HttpHeaders headers) {
		String nextLink = null;
		List<String> links = headers.get("Link");
		if(links.size() > 0) {
			String[] linkSections = links.get(0).split(",");
			for(String link : linkSections) {
				if(link.endsWith("; rel=\"next\"")) {
					nextLink = link.replace("; rel=\"next\"", "");
					nextLink = nextLink.replace("<", "");
					nextLink = nextLink.replace(">", "");
					System.out.println("next link: " + nextLink);
				}
			}
		}
	return nextLink;
	}
}
