package BotTools.botactions.online;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * This class handles the lowest level online actions.
 * These are not bot actions and this class does not interact
 * as a bot with discord api. It only contains internet 
 * browsing logic. For bot actions that use online data see
 * this class' wrapper class OnlineAction. 
 * @see OnlineAction
 * @author Spartak
 *
 */
class Connector{
	private static final Logger log = LoggerFactory.getLogger(Connector.class);
	
	private BufferedReader reader;
	private URLConnection connection;
	private volatile boolean isOpen;
	/** 
	 * The current line of the html of the most recently
	 * established connection. 
	 */
	protected String data;
	/** Creates a new Connector */
	Connector(){
		isOpen = false;
		Unirest.setTimeouts(0, 0);
		log.info("Connector was created successfully");
	}
	/**
	 * Opens a new connection with the given URL.
	 * If another {@link #open(String)} call is made before the current 
	 * connection is closed with {@link #close()} then the new connection 
	 * will wait indefinitely until the current connection closes.
	 * @param urlName the url that you are trying to access
	 * @return Whether a connection was successfully established
	 */
	final synchronized boolean open(String urlName){
		log.info("Starting attempt to connect to url: {}", urlName);
		waitForTurn();
		URL url = getURL(urlName);
		// This is to try to appear as a browser user since some websites reject the connection otherwise
		System.setProperty("http.agent", "Mozilla/4.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		connection = openConnection(url);
		isOpen = true;
		createReader();
		return readerState();
	}
	/**
	 * Blocks the thread if there is a connection that is still open. 
	 * If a second connection is opened, the first one may close the reader
	 * prior to the second one being done using it.
	 */
	private void waitForTurn() {
		while(isOpen) {
			log.trace("Waiting on current connection to: {} to close", connection.getURL());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("An error occured while waiting for a connection to close");
				e.printStackTrace();
			}
		}
		log.debug("A connection is ready to be opened");
	}
	private URL getURL(String urlName) {
		try {
			URL url = new URL(urlName);
			log.trace("URL formed successfully");
			return url;
		}catch(MalformedURLException e) {
			log.error("Invalid URL provided: {}", urlName);
			throw new IllegalArgumentException("Given URL is not valid");
		}
	}
	private URLConnection openConnection(URL url) {
		log.trace("Attempting to open connection");
		try {
			URLConnection connection = url.openConnection();
			log.trace("Successfully opened a url connection");
			return connection;
		} catch (IOException e) {
			log.error("A connection to the URL: {} could not be opened", url);
			e.printStackTrace();
			return null;
		}
	}
	private void createReader() {
		log.trace("Attempting to create a Buffered Reader from an opened URL connection");
		try {
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is));
			log.trace("A reader was successfully created");
		}catch(IOException e) {
			log.error("Could not create a reader for the url connection: {}", connection.getURL());
			e.printStackTrace();
		}
	}
	private boolean readerState() {
		log.trace("Checking reader state");
		try {
			if(reader == null) {
				log.error("A newly opened connection's reader is null");
				return false;
			}
			if(!reader.ready()) {
				log.error("A reader was created but is not ready");
				return false;
			}
			log.trace("Reader was validated");
			return true;
		} catch (IOException e) {
			log.warn("An error occured while checking the reader state");
			return false;
		}
	}
	
	/**
	 * Closes the currently established connection. Must be called before another connection can be established.
	 */
	final void close() {
		try {
			reader.close();
			isOpen = false;
		} catch (IOException e) {
			log.error("An error occurred while trying to close the reader.");
			e.printStackTrace();
		}
	}
	/**
	 * Saves the Image at the given url to a file with the given name
	 * @param imageUrl - the url of the image that you are trying to send
	 * @param destinationFile - the file to which you are trying to save an image to
	 * @return The file to which the image was saved.
	 */
	protected final File saveImage(String imageUrl, String destinationFile){
		log.info("Saving an image from url: {},\tto file: {}", imageUrl, destinationFile);
		try {
		    open(imageUrl);
		    InputStream is = connection.getInputStream();
		    OutputStream os = new FileOutputStream(destinationFile);
	
		    byte[] b = new byte[2048];
		    int length;
	
		    while ((length = is.read(b)) != -1)
		        os.write(b, 0, length);
	
			is.close();
			os.close();
			close();
		}catch(IOException e) {
			log.error("Could not save image from url: " + imageUrl);
			e.printStackTrace();
		}
		
		return new File(destinationFile);
	}
	/**
	 * Skip the specified number of lines in the html data of the 
	 * most recently established connection
	 * @param lines - the number of lines that you want skipped
	 * @throws IOException if there aren't that many lines left in the html
	 */
	final void next(int lines) throws IOException{
		while(lines-- > 0)
			next();
	}
	/**
	 * Goes to the next line in the html data of the most
	 * recently established connection
	 * @throws IOException if there are no more lines left
	 */
	final void next() throws IOException{
		data = reader.readLine();
	}
	/**
	 * Goes to the next line in the html data of the most
	 * recently established connection and returns the value 
	 * of that line as a new String
	 * @return the next line 
	 * @throws IOException if there are no more lines left
	 */
	String get() throws IOException{
		next();
		return new String(data);
	}
	/**
	 * Performs a GET request on a given URL
	 * @param URL that you want to send a GET request to
	 * @return the result of the GET request as a String
	 * or null if unable to GET
	 */
	String get(String URL) {
		log.debug("Attempting a GET action on url: {}", URL);
		try {
			return Unirest.get(URL).asString().getBody();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}
}