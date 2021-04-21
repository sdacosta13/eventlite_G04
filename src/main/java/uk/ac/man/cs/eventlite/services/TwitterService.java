package uk.ac.man.cs.eventlite.services;

import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Service
public class TwitterService {
	private static final String CONSUMER_KEY_SECRET = "GFAKktZnJ5yELhNjceyzbN9qIDYgmBUG08nF4Kh9JS7BNZ8O9H";
	private static final String CONSUMER_KEY = "O0uL2umfLrg4XB0ug3634ErGo";
	private static final String ACCESS_TOKEN_SECRET = "s6WSADgcJkS0fxONMVYSS0M3yv8MthOrX6U2ncOmt2iF0";
	private static final String ACCESS_TOKEN = "1381513512799895555-9smb9E4Sa0vWFyjAvt8NdwicJ61Xqc";
	private static final String BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAIOFOgEAAAAA06V4C1LTEIduoox4K8pPGI8I4UI%3DzWHddHbXtwsp9UkXa4l27S3YdvxpWvMIgqgZkm1IGqernKF7rS";
	private Twitter twitter;
	
	public TwitterService() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(CONSUMER_KEY)
		.setOAuthConsumerSecret(CONSUMER_KEY_SECRET)
		.setOAuthAccessToken(ACCESS_TOKEN)
		.setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	
	public Twitter getTwitter() {
		return twitter;
	}
	
	public boolean postTweet(String tweet) {
		try {
			twitter.updateStatus(tweet);
			return true;
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return false;
	}
}
