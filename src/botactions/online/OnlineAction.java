package botactions.online;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.json.JSONObject;
import org.json.JSONTokener;

import botactions.BotAction;
import net.dv8tion.jda.api.entities.TextChannel;

public final class OnlineAction extends BotAction {
	private static final Connector connector = new Connector();
	/**
	 * Sends an image found online at the designated URL to the passed in Text channel
	 * @param imageAddress the URL of the image that is to be sent
	 * @param channel the Text channel that the image is to be sent to
	 */
	public static void sendImageInChannel(String imageAddress, TextChannel channel) {
		sendFileInChannel(connector.saveImage(imageAddress, "res/temp.png"), channel);
	}
	/**
	 * Returns the next line of the source HTML code from the URL you are currently 
	 * connected to
	 * @return the next line of HTML source code
	 * @throws IOException
	 */
	public static String getNextLine() throws IOException {
		return connector.get();
	}
	/**
	 * Establishes a connection with the specified URL. Return whether the
	 * established connection was successful or not.
	 * @param URL the URL address that you want to connect to.
	 * @return <b>true</b> is the connection was established successfully <br>
	 * <b>false</b> if it was not
	 */
	public static boolean connect(String URL) {
		return connector.open(URL);
	}
	/**
	 * Return the results of a get request sent to the specified URL
	 * @param URL to send a get request to
	 * @return what is returned by the get request
	 */
	public static JSONObject get(String URL) {
		Reader reader = new StringReader(connector.get(URL));
		return new JSONObject(new JSONTokener(reader));
	}
	/**
	 * Closes the connection to the currently opened URL. 
	 * Must be done before opening another one.
	 */
	public static void closeConnection() {
		connector.close();
	}
}
