package dataprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import org.w3c.dom.*;

import com.bottools.main.Botmain;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class GuildDataParser {
	private XMLReader reader = new XMLReader();
	private String guildPath;
	private PlaylistDecoder playlistDecoder = new PlaylistDecoder();
	private final boolean debug = false;
	public GuildDataParser(){
		//updateGuildMemberDatabase();
	}
	public Double getPromotionPoints(Guild guild, String memberName){
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node person = getPersonNode(memberName);
		Botmain.debug("The retrived person node is: " + (person == null ? "" : "not ") + "null");
		Node points = getPointsNode(person);
		Botmain.debug("Retrieved the points node: " + points);
		return Double.parseDouble(reader.getValue(points));
	}
	public synchronized void updateServerDatabase(){
		// DO NOT DELETE    use if the infinite whitespace in xml files bug occurs again
		//for(Guild g: Botmain.jda.getGuilds())Trimmer.trim("C:\\Users\\spart\\OneDrive\\Documents\\Programming\\workspace\\TrumpBotTest\\guildData\\" + g.getName() + "\\MemberData.xml");
		
		for(Guild g: Botmain.jda.getGuilds())
			updateServerDatabase(g);
		Botmain.out("Finished updating server member database");
	}
	
	private void updateServerDatabase(Guild guild) {	
		ArrayList<String> filedMembers = attemptRetrieveNames(guild);
		List<Member> actualMembers = guild.loadMembers().get();
		for(Member m: actualMembers){						
			String personName = m.getUser().getName();
			Botmain.debug("Found guild member named: " + personName);
			boolean isInSystem = filedMembers.contains(personName);
			Botmain.debug("This member is " + (isInSystem ? "" : "not ") + "in the system");
			if(!isInSystem){
				Element personTag = reader.newChild(reader.getRoot(), "Person");
				reader.newChild(personTag, "tName", personName);
				reader.newChild(personTag, "nPoints", "0.0");
				reader.newChild(personTag, "time", "");
				Node timeNode = reader.getChild(personTag, "time");
					reader.newChild(timeNode, "timezone", "none");
					reader.newChild(timeNode, "dston", "false");
					reader.newChild(timeNode, "dststart", "none");
					reader.newChild(timeNode, "dstend", "none");
				reader.newChild(personTag, "match", "none");
				reader.newChild(personTag, "requestedby", "none");
				reader.newChild(personTag, "money", "1000");
				reader.newChild(personTag, "IDs");
			}
		}
	}
	
	public void setDSTon(Guild guild, Member member, boolean on) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node person = getPersonNode(member.getUser().getName());
		Node dston = reader.getChild(reader.getChild(person, "time"), "dston");
		reader.setValue(dston, on ? "on" : "off");
	}
	public boolean isDSTTrackingOn(Guild guild, Member member) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node person = getPersonNode(member.getUser().getName());
		Node dston = reader.getChild(reader.getChild(person, "time"), "dston");
		return reader.getValue(dston).toLowerCase().equals("on");
	}
	
	private ArrayList<String> attemptRetrieveNames(Guild guild){
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		try{
			return retriveNames();
		}catch(Exception e){
			System.out.println("Could not retrieve names for the guild " + guild.getName());
			e.printStackTrace();
			return new ArrayList<String>(1);
		}
	}
	public HashMap<Integer, ImmutablePair<String, Double>> getRankReqs(Guild guild){
		setGuildString(guild);
		reader.setDocument(guildPath + "autopromotionranks.xml");
		HashMap<Integer, ImmutablePair<String, Double>> reqs = new HashMap<Integer, ImmutablePair<String,Double>>();
		Node rank = reader.stepDown(reader.getRoot());
		if(rank == null)
			return reqs;
		String rankName;
		Double pointReq;
		Node nameNode;
		int i = 0;
		reqs.put(i++, new ImmutablePair<String,Double>(getDefaultRole(guild).getName(),0.0));
		do{
			nameNode = reader.stepDown(rank);
			rankName = reader.getValue(nameNode);
			pointReq = Double.parseDouble(reader.getValue(reader.stepOver(nameNode)));
			Botmain.debug("Putting: " + rankName + ", " + pointReq);
			reqs.put(i++, new ImmutablePair<String, Double>(rankName, pointReq));
		}while((rank = reader.stepOver(rank)) != null);
		return reqs;
	}
	public double addPoints(User user, double amount){
		Botmain.debug("Starting addPoints() with: " + user.getName());
		Node person = getPersonNode(user.getName());
		Botmain.debug("Person: " + person.getNodeName());
		Node pointsNode = getPointsNode(person);
		double currentAmount = Double.parseDouble(reader.getValue(pointsNode));
		Botmain.debug("Points: " + currentAmount);
		reader.setValue(pointsNode, String.valueOf(amount + currentAmount));
		double finalValue = Double.parseDouble(reader.getValue(pointsNode));
		Botmain.debug("Final value: " + finalValue);
		return finalValue;
	}
	public void setPoints(User user, double amount){
		Node person = getPersonNode(user.getName());
		Node pointsNode = getPointsNode(person);
		double currentAmount = Double.parseDouble(reader.getValue(pointsNode));
		reader.setValue(pointsNode, String.valueOf(amount));
	}
	public Role getDefaultRole(Guild guild) {
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		List<Role> roleList = guild.getRolesByName(reader.getValue(reader.stepDown(reader.getRoot())), true);
		return roleList.size() > 0 ? roleList.get(0) : guild.getPublicRole();
	}
	public String getWelcomeMessage(Guild guild){
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node root = reader.getRoot();
		Node defaultRole = reader.stepDown(root);
		Node welcomeMessage = reader.stepOver(defaultRole);
		return reader.getValue(welcomeMessage);
	}
	public String getWelcomeMessageChannel(Guild guild){
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		int index = 3;
		Node node = reader.stepDown(reader.getRoot());
		for(int i = 1; i < index; i++)
			node = reader.stepOver(node);
		return reader.getValue(node);
	}
	public void setWelcomeMessage(Guild guild, String message){
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node node = reader.stepDown(reader.getRoot());
		node = reader.stepOver(node, 2);
		reader.setValue(node, message);
	}
	private ArrayList<String> retriveNames(){
		ArrayList<String> names = new ArrayList<String>();
		Node root = reader.getRoot();							// The top level element in the document
		Botmain.debug("Root: " + root.getNodeName());		// Debug info
		Node person = reader.stepDown(root);
		Node memberName;										
		do{
			memberName = reader.stepDown(person);				// Gets the <name> tag
			String nodeValue = reader.getValue(memberName);		// Gets the name text value
			names.add(nodeValue);								// Adds the name to the list of names
			if(debug)
				System.out.println("Found person: " + nodeValue + " in the database");
		}while((person = reader.stepOver(person)) != null);
		return names;
	}
	private Node getPersonNode(String username){
		username = username.toLowerCase();
		Botmain.debug((char)31 + "Starting getPersonNode function with: " + username);
		reader.setDocument(guildPath + "MemberData.xml");
		Node person = getFirstPerson();
		Botmain.debug((char)32 + "First person is: " + reader.getValue(reader.stepDown(person)));
		while(person != null){
			Node nameNode = reader.stepDown(person);
			String name = reader.getValue(nameNode).toLowerCase();
			Botmain.debug("Found person: " + name);
			if(name.toLowerCase().equals(username))
				break;
			person = reader.stepOver(person);
		}
		Botmain.debug("Ending getPersonNode function with: " + username);
		return person;
	}
	private Node getFirstPerson(){
		return reader.stepDown(reader.getRoot());
	}
	private Node getPointsNode(Node personNode){
		Botmain.debug("Starting getPointsNode function with: " + personNode);
		Node nameTag = reader.stepDown(personNode);
		Botmain.debug("getPointsNode() Teh naem of the person is: " + reader.getValue(nameTag));
		Node pointsNode = reader.stepOver(nameTag);
		Botmain.debug("getPointsNode() returning " + pointsNode);
		return pointsNode;
	}
	private void setGuildString(Guild guild){
		guildPath = "guildData/" + guild.getName() + "/";	
	}
	
	public void setPlaylist(Guild guild, String playlistName){
		playlistDecoder.setPlayList(guild, playlistName);
	}
	public String nextSong(){
		return playlistDecoder.getNextSong();
	}
	public void makePlaylist(Guild guild, String playlistName){
		playlistDecoder.makePlaylist(guild, playlistName);
	}
	public void addSong(Guild guild, String name){
		playlistDecoder.addSong(guild, name);
	}
	class PlaylistDecoder{
		private String playlist;
		private int onSong;
		void setPlayList(Guild guild, String playlist){
			this.playlist = playlist;
			onSong = 0;
			setDoc(guild);
		}
		String getNextSong(){
			Node node = findPlaylist(playlist);
			node = reader.stepDown(node);
			node = reader.stepOver(node, onSong++);
			return node == null ? null : reader.getValue(node);
		}
		void makePlaylist(Guild guild, String playlist){
			setDoc(guild);
			reader.newChild(reader.getRoot(), "Playlist", playlist);
		}
		void addSong(Guild guild, String name){
			setDoc(guild);
			reader.newChild(findPlaylist(playlist), "song", name);
		}
		ArrayList<String> listPlaylists(Guild guild){
			ArrayList<String> playlists = new ArrayList<String>();
			setDoc(guild);
			Node playlist = reader.stepDown(reader.getRoot());
			while(playlist != null){
				playlists.add(reader.getValue(playlist));
				playlist = reader.stepOver(playlist);
			}
			return playlists;
		}
		private Node findPlaylist(String name){
			Node node;
			name = name.toLowerCase();
			for(node = reader.stepDown(reader.getRoot()); node != null && !reader.getValue(node).toLowerCase().startsWith(name); node = reader.stepOver(node))
				Botmain.debug("Found playlist: " + reader.getValue(node));
			return node;
		}
		private void setDoc(Guild guild){
			setGuildString(guild);
			reader.setDocument(guildPath + "Playlists.xml");
		}
		ArrayList<String> listSongs(Guild guild, String playlistName) {
			setDoc(guild);
			ArrayList<String> songNames = new ArrayList<String>();
			Node playlist = findPlaylist(playlistName);
			Node song = reader.stepDown(playlist);
			while(song != null){
				songNames.add(reader.getValue(song));
				song = reader.stepOver(song);
			}
			return songNames;
		}
		
	}
	public ArrayList<String> listPlaylists(Guild guild) {
		return playlistDecoder.listPlaylists(guild);
	}
	public ArrayList<String> listSongs(Guild guild, String string) {
		return playlistDecoder.listSongs(guild, string);
	}
	public String getTimeZone(Guild guild, Member member) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		Node timeNode = reader.getChild(personNode, "time");
		Node timezone = reader.getChild(timeNode, "timezone");
		return reader.getValue(timezone);
	}
	public void setTimeZone(Guild guild, Member member, int timezone){
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		Node timeNode = reader.getChild(personNode, "time");
		Node timezoneNode = reader.getChild(timeNode, "timezone");
		reader.setValue(timezoneNode, String.valueOf(timezone));
	}
	public void setDST(Guild guild, Member member, boolean start, String rawInput) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		Node timeNode = reader.getChild(personNode, "time");
		Node dstNode = reader.getChild(timeNode, start ? "dststart" : "dstend");
		reader.setValue(dstNode, rawInput);
	}
	public String getDST(Guild guild, Member member, boolean start) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		Node timeNode = reader.getChild(personNode, "time");
		Node dstNode = reader.getChild(timeNode, start ? "dststart" : "dstend");
		return reader.getValue(dstNode);		
	}
	
	public void addDuplicatedChannel(Guild own, Guild source, TextChannel channel) {
		setGuildString(own);
		reader.setDocument(guildPath + "DuplicatedChannels.xml");
		Node root = reader.getRoot();
		String id = source.getId().replaceAll("\n", "");
		Node sourceGuildNode = null;
		boolean foundExisting = false;
		for(Node child: reader.getChildren(reader.getRoot())) {
			if(reader.getValue(child).replaceAll("\n", "").equals(id)) {
				foundExisting = true;
				sourceGuildNode = child;
				break;
			}
		}
		if(!foundExisting)
			sourceGuildNode = reader.newChild(root, "Server", id);
		reader.newChild(sourceGuildNode, "Channel", channel.getId());
	}
	
	public HashMap<Guild, HashMap<Guild, ArrayList<TextChannel>>> getDuplicates(){
		HashMap<Guild, HashMap<Guild, ArrayList<TextChannel>>> duplicates = new HashMap<Guild, HashMap<Guild, ArrayList<TextChannel>>>();
		for(Guild g: Botmain.jda.getGuilds()) {
			duplicates.put(g, new HashMap<Guild, ArrayList<TextChannel>>());
			setGuildString(g);
			reader.setDocument(guildPath + "DuplicatedChannels.xml");
			for(Node n: reader.getChildren(reader.getRoot())) {
				Guild sourceGuild = Botmain.jda.getGuildById(reader.getValue(n).replaceAll("\n", "").trim());
				duplicates.get(g).put(sourceGuild, new ArrayList<TextChannel>());
				for(Node channelNode: reader.getChildren(n)) {
					duplicates.get(g).get(sourceGuild).add(sourceGuild.getTextChannelById(reader.getValue(channelNode)));
				}
			}
		}
		return duplicates;
	}
	
	public void addTag(String fileName, String name, String value) {
		for(Guild guild: Botmain.jda.getGuilds()) {
			setGuildString(guild);
			reader.setDocument(guildPath + fileName + ".xml");
			Node parentNode = null;
			switch(fileName.toLowerCase()) {
			case "memberdata":
				parentNode = reader.stepDown(reader.getRoot());
				break;
			case "guilddata":
				parentNode = reader.getRoot();
				break;
			default: throw new IllegalArgumentException("invalid file name");
			}
			do {
				reader.newChild(parentNode, name, value);
			}while((parentNode = reader.stepOver(parentNode)) != null);
		}
	}
	public void removeTag(String fileName, String tag) {
		for(Guild guild: Botmain.jda.getGuilds()) {
			setGuildString(guild);
			reader.setDocument(guildPath + fileName + ".xml");Node parentNode = null;
			switch(fileName.toLowerCase()) {
			case "memberdata":
				parentNode = reader.stepDown(reader.getRoot());
				break;
			case "guilddata":
				parentNode = reader.getRoot();
				break;
			default: throw new IllegalArgumentException("invalid file name");
			}
			do {
				reader.removeTag(parentNode, tag);
			}while((parentNode = reader.stepOver(parentNode)) != null);
		}
	}
	public String getLastMatchID(Guild guild, Member member) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node person = getPersonNode(member.getUser().getName());
		Node id = reader.getChild(person, "match");
		return reader.getValue(id);
	}
	public void setMatch(Guild guild, Member member, String newID) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node person = getPersonNode(member.getUser().getName());
		Node id = reader.getChild(person, "match");
		reader.setValue(id, newID);
	}
	public String getD2MatchChannel(Guild guild) {
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node channelIDNode = reader.getChild(reader.getRoot(), "d2matchinfochannel");
		return reader.getValue(channelIDNode);
	}
	public void setD2MatchChannel(Guild guild, String channelName) {
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node channelIDNode = reader.getChild(reader.getRoot(), "d2matchinfochannel");
		System.out.println("Before: " + reader.getValue(channelIDNode));
		reader.setValue(channelIDNode, channelName);
		System.out.println("After: " + reader.getValue(channelIDNode));
	}
	public void addRequest(Guild guild, Member requested, Member requester) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		
		String requestedName = requested.getUser().getName(), requesterName = requester.getUser().getName();
		
		Node requestedPersonNode = getPersonNode(requestedName);
		Node requestedby = reader.getChild(requestedPersonNode, "requestedby");
		if(reader.getValue(requestedby).equals("none") || reader.getValue(requestedby).equals(""))
			reader.setValue(requestedby, requesterName + " ");
		else 
			reader.setValue(requestedby, reader.getValue(requestedby).concat(requesterName + " "));
	}
	public void removeRequest(Guild guild, Member requested, Member requester) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		
		String requestedName = requested.getUser().getName(), requesterName = requester.getUser().getName();
		
		Node personNode = getPersonNode(requestedName);
		Node requestedby = reader.getChild(personNode, "requestedby");
		String requests = reader.getValue(requestedby);
		requests.replaceFirst(requesterName + " ", "");
	}
	public String getRequesters(Guild guild, User requested) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		
		Node personNode = getPersonNode(requested.getName());
		return reader.getValue(reader.getChild(personNode, "requestedby"));
	}
	public String[] getContents(Guild guild, String person) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		
		Node personNode = getPersonNode(person);
		NodeList contents = personNode.getChildNodes();
		String[] names = new String[contents.getLength()];
		
		for(int i = 0; i < contents.getLength(); i++)
			names[i] = contents.item(i).getNodeName();
		return names;
	}
	public void removeSongFromPlaylist(Guild guild, String song, String playlist) {
		setGuildString(guild);
		reader.setDocument(guildPath + "Playlists.xml");
		
		Node root = reader.getRoot();
		Node playlistNode = null;
		for(Node node: reader.getChildren(root))
			if(reader.getValue(node).equals(playlist))
				playlistNode = node;
		reader.removeTagByText(playlistNode, song);
	}
	public String getRecordedPatch() {
		reader.setDocument("BotData.xml");
		Node root = reader.getRoot();
		Node patchNode = reader.getChild(root, "dotapatch");
		return reader.getValue(patchNode);
	}
	public void setRecordedPatch(String patch) {
		reader.setDocument("BotData.xml");
		Node root = reader.getRoot();
		Node patchNode = reader.getChild(root, "dotapatch");		
		reader.setValue(patchNode, patch);
	}
	public String getDotaPatchNotesChannel(Guild guild) {
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node root = reader.getRoot();
		Node channel = reader.getChild(root, "dotapatchnoteschannel");
		return reader.getValue(channel);
	}
	public void setDotaPatchNotesChannel(Guild guild, String channelName) {
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node root = reader.getRoot();
		Node channel = reader.getChild(root, "dotapatchnoteschannel");
		reader.setValue(channel, channelName);
	}
	public String getCatCounter() {
		reader.setDocument("Botdata.xml");
		Node counterNode = reader.getChild(reader.getRoot(), "catcounter");
		String counterText = reader.getValue(counterNode);
		int counter = Integer.parseInt(counterText) + 1;
		reader.setValue(counterNode, String.valueOf(counter));
		return counterText;
		
	}
	public void setGreetActive(Guild guild, String valueOf) {
		setGuildString(guild);
		reader.setDocument(guildPath + "GuildData.xml");
		Node greetingActive;
	}
	public void setGameID(Guild guild, Member member, String gameName, String memberID) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		Node id = reader.getChild(personNode, "IDs");
		Node gameNode = reader.getChild(id, gameName);
		if(gameNode == null)
			reader.newChild(id, gameName, memberID);
		else reader.setValue(gameNode, memberID);
	}
	public String getID(Guild guild, Member member, String gameName) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		Node id = reader.getChild(personNode, "IDs");
		Node gameNode = reader.getChild(id, gameName);
		return gameNode == null ? null : reader.getValue(gameNode);
	}
	public void setMoney(Guild guild, Member member, int value) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		reader.setValue(reader.getChild(personNode, "money"), String.valueOf(value));
	}
	public int getMoney(Guild guild, Member member) {
		setGuildString(guild);
		reader.setDocument(guildPath + "MemberData.xml");
		Node personNode = getPersonNode(member.getUser().getName());
		if(!reader.nodeHasChild(personNode, "money"))
			reader.newChild(personNode, "money", "1000");
		return Integer.parseInt(reader.getValue(reader.getChild(personNode, "money")));
	}
	
	public void setWelcomeMessageID(Guild guild, TextChannel channel, Message message) {
		setDocument(guild, "GuildData.xml");
		Node parent = reader.getChild(reader.getRoot(), "welcomeMessages");
		Node id = reader.newChild(parent, "welcomeMessage");
		reader.newChild(id, "channel", channel.getId());
		reader.newChild(id, "message", message.getId());
	}
	
	public List<Pair<String, String>> getWelcomeMessageIDs(Guild guild) {
		setDocument(guild, "GuildData.xml");
		ArrayList<Pair<String, String>> welcomeMessageIDs = new ArrayList<Pair<String, String>>();
		reader.getChildren(reader.getChild(reader.getRoot(), "welcomeMessages")).forEach(parent ->{
			Node channel = reader.getChild(parent, "channel");
			Node message = reader.getChild(parent, "message");
			welcomeMessageIDs.add(Pair.of(reader.getValue(channel), reader.getValue(message)));
		});
		return welcomeMessageIDs;
	}
	
	public void addReactionRole(Guild guild, Emote emote, Role role) {
		setDocument(guild, "GuildData.xml");
		Node rrsNode = reader.getChild(reader.getRoot(), "reactionRoles"),
			 rrNode = reader.newChild(rrsNode, "reactionRole");
		reader.newChild(rrNode, "emote", emote.getId());
		reader.newChild(rrNode, "role", role.getId());
	}
	
	public List<String> getGameEmoteIDs(Guild guild){
		setDocument(guild, "GuildData.xml");
		Node rrsNode = reader.getChild(reader.getRoot(), "reactionRoles");
		ArrayList<String> emoteIDs = new ArrayList<String>();
		reader.getChildren(rrsNode).forEach(combination ->
				emoteIDs.add(reader.getValue(reader.getChild(combination, "emote"))));
		return emoteIDs;
	}
	
	public String getCorrespondingRoleID(Guild guild, Emote emote) {
		setDocument(guild, "GuildData.xml");
		Node rrsNode = reader.getChild(reader.getRoot(), "reactionRoles");
		for(Node n: reader.getChildren(rrsNode))
			if(reader.getValue(reader.getChild(n, "emote")).equals(emote.getId()))
				return reader.getValue(reader.getChild(n, "role"));
		return null;
	}
	
	private void setDocument(Guild guild, String docName) {
		setGuildString(guild);
		reader.setDocument(guildPath + docName);
	}
	
	public void createGuildDatabase(Guild guild){
		setGuildString(guild);
		new File(guildPath).mkdirs();
		
		List<String> guildData = Arrays.asList("<data>","<defaultRole>online</defaultRole>", 
				"<welcomeMessage>hello</welcomeMessage>",
				"<d2matchinfochannel>none</d2matchinfochannel>", "<dotapatchnoteschannel>none</dotapatchnoteschannel>", "</data>",
				"<welcomeMessages></welcomeMessages>", "<reactionRoles></reactionRoles>" );
		Path guildDataPath = Paths.get(guildPath + "GuildData.xml");
		
		List<String> people = Arrays.asList("<People>\n", "</People>");
		Path peoplePath = Paths.get(guildPath + "MemberData.xml");
		
		List<String> ranks = Arrays.asList("<Enlisted>\n","</Enlisted>");
		Path ranksPath = Paths.get(guildPath + "AutoPromotionRanks.xml");
		
		List<String> duplicatedChannels = Arrays.asList("<Duplicates>\n","</Duplicates>");
		Path duplicatesPath = Paths.get(guildPath + "DuplicatedChannels.xml");
		
		try {
			Files.write(guildDataPath, guildData, Charset.forName("UTF-8"));
			Files.write(peoplePath, people, Charset.forName("UTF-8"));
			Files.write(ranksPath, ranks, Charset.forName("UTF-8"));
			Files.write(duplicatesPath, duplicatedChannels, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//updateServerDatabase(guild);
	}
	public void modBotDataValue(String name, String value) {
		reader.setDocument("BotData.xml");
		Node node = reader.getChild(reader.getRoot(), name);
		reader.setValue(node, value);
	}
	public String getBotDataValue(String name) {
		reader.setDocument("BotData.xml");
		Node node = reader.getChild(reader.getRoot(), name);
		return reader.getValue(node);
	}
}
