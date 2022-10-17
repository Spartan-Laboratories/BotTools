package BotTools.services;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class RssService extends CheckValueService{

	SyndFeed feed;
	
	public static void createService(String serviceName, Consumer<String> onChange, int interval) {
		createService(new RssService(serviceName), onChange, interval);
	}
	
	protected RssService(String serviceName) {
		super(serviceName);
			try {
				feed = new SyndFeedInput().build(new XmlReader(new URL(packet.getURL())));
			} catch (IllegalArgumentException | FeedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void loop() {
		System.out.println("Testing rss feed service");
		data = feed.getEntries().get(0).getUri();
		checkValues();
	}
}
