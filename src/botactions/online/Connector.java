package botactions.online;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.bottools.main.Botmain;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

class Connector{
	private BufferedReader reader;
	private int openCount;
	private URLConnection connection;
	private boolean isOpen;
	protected String data;
	Connector(){
		openCount = 0;
		isOpen = false;
		Unirest.setTimeouts(0, 0);
	}
	final boolean open(String urlName){
		try {
			/*
			 * Blocks the thread if there is a connection that is still open. 
			 * If a second connection is opened, the first one may close the reader
			 * prior to the second one being done using it.
			 */
			while(isOpen);

			isOpen = true;
			URL url = new URL(urlName);
			System.setProperty("http.agent", "Mozilla/4.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			connection = url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");;
			openCount++;
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			Botmain.out("(" + openCount + ")Successfully opened url: " + urlName);
			return true;
		}catch(IOException e) {
			String errorMessage = "Could not connect to url: " + urlName;
			System.out.println(errorMessage);
			e.printStackTrace();
			return false;
		}
	}
	final void close() {
		//*
		try {
			reader.close();
			isOpen = false;
			//Botmain.out("Closed url: " + getURL());
		} catch (IOException e) {
			System.out.println("An error occurred while trying to close the reader.");
			e.printStackTrace();
		}
		//*/
	}
	
	protected final File saveImage(String imageUrl, String destinationFile){
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
			System.out.println("Could not save image from url: " + imageUrl);
			e.printStackTrace();
		}
		
		return new File(destinationFile);
	}
	final void next(int lines) throws IOException{
		while(lines-- > 0)
			next();
	}
	final void next() throws IOException{
		data = reader.readLine();
	}
	String get() throws IOException{
		next();
		return new String(data);
	}
	final URLConnection getConnection() {
		return connection;
	}
	String get(String URL) {
		try {
			return Unirest.get(URL).asString().getBody();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}
}