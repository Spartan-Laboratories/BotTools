package botactions.online;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.json.JSONObject;
import org.json.JSONTokener;

import botactions.BotAction;
import net.dv8tion.jda.api.entities.TextChannel;
/**
 * An extension of the BotAcion class adding functionality that has
 * to do with accessing online content
 * @author Spartak
 *
 */
public final class OnlineAction extends BotAction {
	private static final Connector connector = new Connector();
	/**
	 * Sends an image found online at the designated URL to the passed in Text channel
	 * @param imageAddress the URL of the image that is to be sent
	 * @param channel the Text channel that the image is to be sent to
	 */
	public static void sendImageInChannel(String imageAddress, TextChannel channel) {
		sendFileInChannel(saveImage(imageAddress, "res/temp.png"), channel);
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
	/**
	 * Takes the image at the given URL and saves it to a file with the given file name.
	 * File does not need to already exist. If it does not exist, then one will be created.
	 * @param url where the image is located
	 * @param fileName the name of the file to which you want to save the image.
	 * @return
	 */
	public static File saveImage(String url, String fileName) {
		return connector.saveImage(url, fileName);
	}
	public static String skipLinesTo(String lineStartSearchTerm) throws IOException {
		String data;
		do {
			data = getNextLine();
		}while(!(data.startsWith(lineStartSearchTerm) || data.trim().startsWith(lineStartSearchTerm)));
		return data;
	}
}
