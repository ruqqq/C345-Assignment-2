/*
 * Class contains static methods for interacting with C345 Assignment 2 Webservice
 * ...the request are standard and normal:
 * ...	the requests are only done in TrendsActivity (2 request)
 * ...	, submitting of song. one per each song
 * ...  and widget refreshing, every 30 minutes
 * ...hence, the speed of retrieval/server quality depends on efficiency of SQL queries on server side
 */
package sg.ruqqq.socialtunes;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sg.ruqqq.socialtunes.item.Trend;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class Webservice {
	// Contants for accessing web service
	static String api_key = "92f710f56cd8d20c76d04874198470eab8713826";
	static String base_request_uri = "http://sit.rp.edu.sg/c345/";
	
	// Methods for generating request URI
	static public String makeRequestUri(String request) {
		return makeRequestUri(Webservice.api_key, request);
	}
	
	static public String makeRequestUri(String request, String params) {
		return makeRequestUri(api_key, request, params);
	}
	
	static public String makeRequestUri(String api_key, String request, String params) {
		String uri = base_request_uri;
		uri = uri+request+"?apikey="+api_key+params;
		return uri;
	}
	
	// Methods for submitting songs to web service
	static public boolean submitSong(String song, String artist) {
		return submitSong(song, artist, null, 0, 0);
	}
	
	static public boolean submitSong(String song, String artist, String album) {
		return submitSong(song, artist, album, 0, 0);
	}
	
	static public boolean submitSong(String song, String artist, String album, double lat, double lng) {
		// Get the XML
        URL url;
        try {
        	// Generat URL for submission
        	String params = "";
        	params = params+"&song="+URLEncoder.encode(song);
        	params = params+"&artist="+URLEncoder.encode(artist);
        	if (album != null) params = params+"&album="+URLEncoder.encode(album);
        	if (lat != 0) params = params+"&lat="+URLEncoder.encode(""+lat);
        	if (lng != 0) params = params+"&lng="+URLEncoder.encode(""+lng);
        	
        	String StringUrl = makeRequestUri("submitsong.php", params);
        	
        	Log.d("WebService", "Submitting to: "+StringUrl);
        	
        	url = new URL(StringUrl);
        	
        	URLConnection connection;
        	connection=url.openConnection();
        	
        	// Starts a HTTP connection
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
          	int responseCode = httpConnection.getResponseCode();
          	
          	// Standard response for successful HTTP requests
          	if (responseCode == HttpURLConnection.HTTP_OK){
          		InputStream in = httpConnection.getInputStream();
          		Log.d("WebService", "Result: "+in.toString());
          		return true;
          	}
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        } finally {
        	
    	}
		
		return false;
	}
	
	// Methods for getting recentPopularSongs
	static public ArrayList<Trend> recentPopularSongs() {
		return recentPopularSongs(0, 0);
	}
	
	static public ArrayList<Trend> recentPopularSongs(int timeframe) {
		return recentPopularSongs(timeframe, 0);
	}
	
	static public ArrayList<Trend> recentPopularSongs(int timeframe, int results) {
		ArrayList<Trend> trends = new ArrayList<Trend>();
		
		// Generate request URI
		String params = "";
		if (timeframe > 0) params = params+"&timeframe="+URLEncoder.encode(""+timeframe);
		if (results > 0) params = params+"&results="+URLEncoder.encode(""+results);
		
		// Get the result set from getNodeListResult; See bottom
		NodeList nl = getNodeListResult("getrecentpopularsongs.php", params);

		// Retrieve the child elements
		if (nl != null && nl.getLength() > 0) {
			// If there are more than 1 child, it will go through each of the XML tree of *current_conditions* and retrieve the content from there. This will be more applicable for *weather_forecase*
			for (int i = 0 ; i < nl.getLength(); i++) {
				Element entry = (Element) nl.item(i);

				Trend trend = new Trend();
				trend.setType(0);
				
				// Retrieve the child elements by its various tag
				// Extract the String content of the child elements
				String artist = "<unknown>";
				try {
					artist = ((Element)entry.getElementsByTagName("artist").item(0)).getFirstChild().getNodeValue();
					trend.setArtist(artist);
				} catch (Exception e) {}

				String song = "<unknown>";
				try {
					song = ((Element)entry.getElementsByTagName("song").item(0)).getFirstChild().getNodeValue();
					trend.setSong(song);
				} catch (Exception e) {}

				String album = "<unknown>";
				try {
					album = ((Element)entry.getElementsByTagName("album").item(0)).getFirstChild().getNodeValue();
					trend.setAlbum(album);
				} catch (Exception e) {}

				String playcount = "0";
				try {
					playcount = ((Element)entry.getElementsByTagName("playcount").item(0)).getFirstChild().getNodeValue();
					trend.setPlaycount(playcount);
				} catch (Exception e) {}

				Log.d("WebService", "");
				Log.d("WebService", "	Data: "+trend);
				Log.d("WebService", "");

				trends.add(trend);
			}
		}
		
		return trends;
	}
	
	// Methods for getting nearby songs
	// ..similar to above except for the input format
	static public ArrayList<Trend> nearbySongs(double lat, double lng) {
		return nearbySongs(lat, lng, 0, 0);
	}
	
	static public ArrayList<Trend> nearbySongs(double lat, double lng, int timeframe, int results) {
		ArrayList<Trend> trends = new ArrayList<Trend>();
		
		String params = "";
		params = params+"&lat="+URLEncoder.encode(""+lat);
		params = params+"&lng="+URLEncoder.encode(""+lng);
		if (timeframe > 0) params = params+"&timeframe="+URLEncoder.encode(""+timeframe);
		if (results > 0) params = params+"&results="+URLEncoder.encode(""+results);
		
		NodeList nl = getNodeListResult("getnearbysongs.php", params);

		// Retrieve the child elements
		if (nl != null && nl.getLength() > 0) {
			// If there are more than 1 child, it will go through each of the XML tree of *current_conditions* and retrieve the content from there. This will be more applicable for *weather_forecase*
			for (int i = 0 ; i < nl.getLength(); i++) {
				Element entry = (Element) nl.item(i);

				Trend trend = new Trend();
				trend.setType(1);

				// Retrieve the child elements by its various tag
				// Extract the String content of the child elements
				String artist = "<unknown>";
				try {
					artist = ((Element)entry.getElementsByTagName("artist").item(0)).getAttributeNode("data").getValue();
					trend.setArtist(artist);
				} catch (Exception e) {}

				String song = "<unknown>";
				try {
					song = ((Element)entry.getElementsByTagName("song").item(0)).getAttributeNode("data").getValue();
					trend.setSong(song);
				} catch (Exception e) {}

				String album = "<unknown>";
				try {
					album = ((Element)entry.getElementsByTagName("album").item(0)).getAttributeNode("data").getValue();
					trend.setAlbum(album);
				} catch (Exception e) {}

				String latData = "0";
				try {
					latData = ((Element)entry.getElementsByTagName("lat").item(0)).getAttributeNode("data").getValue();
					trend.setLat(latData);
				} catch (Exception e) {}
				
				String lngData = "0";
				try {
					lngData = ((Element)entry.getElementsByTagName("lng").item(0)).getAttributeNode("data").getValue();
					trend.setLng(lngData);
				} catch (Exception e) {}
				
				String playtime = "0";
				try {
					playtime = ((Element)entry.getElementsByTagName("playtime").item(0)).getAttributeNode("data").getValue();
					trend.setPlaytime(playtime);
				} catch (Exception e) {}
				
				String distance = "0";
				try {
					distance = ((Element)entry.getElementsByTagName("distance").item(0)).getAttributeNode("data").getValue();
					trend.setDistance(distance);
				} catch (Exception e) {}

				Log.d("WebService", "");
				Log.d("WebService", "	Data: "+trend);
				Log.d("WebService", "");

				trends.add(trend);
			}
		}
		
		return trends;
	}
	
	// Methods for getting song last played locations
	// ...similar to above format
	static public ArrayList<Trend> songLocations(String song) {
		return songLocations(song);
	}
	
	static public ArrayList<Trend> songLocations(String song, String artist) {
		ArrayList<Trend> trends = new ArrayList<Trend>();
		
		String params = "";
		params = params+"&song="+song;
		if (artist != null) params = params+"&artist="+artist;
		
		NodeList nl = getNodeListResult("getsonglocations.php", params);

		// Retrieve the child elements
		if (nl != null && nl.getLength() > 0) {
			// If there are more than 1 child, it will go through each of the XML tree of *current_conditions* and retrieve the content from there. This will be more applicable for *weather_forecase*
			for (int i = 0 ; i < nl.getLength(); i++) {
				Element entry = (Element) nl.item(i);

				Trend trend = new Trend();
				trend.setType(2);

				// Retrieve the child elements by its various tag
				// Extract the String content of the child elements
				trend.setArtist(artist);
				trend.setSong(song);

				String album = "<unknown>";
				try {
					album = ((Element)entry.getElementsByTagName("album").item(0)).getAttributeNode("data").getValue();
					trend.setAlbum(album);
				} catch (Exception e) {}

				String latData = "0";
				try {
					latData = ((Element)entry.getElementsByTagName("lat").item(0)).getAttributeNode("data").getValue();
					trend.setLat(latData);
				} catch (Exception e) {}
				
				String lngData = "0";
				try {
					lngData = ((Element)entry.getElementsByTagName("lng").item(0)).getAttributeNode("data").getValue();
					trend.setLng(lngData);
				} catch (Exception e) {}
				
				String playtime = "0";
				try {
					playtime = ((Element)entry.getElementsByTagName("playtime").item(0)).getAttributeNode("data").getValue();
					trend.setPlaytime(playtime);
				} catch (Exception e) {}

				Log.d("WebService", "");
				Log.d("WebService", "	Data: "+trend);
				Log.d("WebService", "");

				trends.add(trend);
			}
		}
		
		return trends;
	}
	
	// Main method for getting data from webservice and return a NodeList for processing
	static public NodeList getNodeListResult(String request, String params) {
        // Get the XML
        URL url;
        NodeList nl = null;
        try {
        	String StringUrl = makeRequestUri(request, params);
        	
        	Log.d("WebService", "Getting from: "+StringUrl);
        	
        	url = new URL(StringUrl);
        	
        	URLConnection connection;
        	connection=url.openConnection();
        	
        	// Starts a HTTP connection
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
          	int responseCode = httpConnection.getResponseCode();
          	
          	// Standard response for successful HTTP requests
          	if (responseCode == HttpURLConnection.HTTP_OK){
          		InputStream in = httpConnection.getInputStream();
          		// A factory API that enables applications to obtain a parser that produces DOM object trees from XML documents
          		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          		DocumentBuilder db = dbf.newDocumentBuilder();
          		
          		// Parse the RSS feed
        		Document dom = db.parse(in);
        		Element docEle = dom.getDocumentElement();
        		
        		nl = docEle.getElementsByTagName("item");
          	}
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (ParserConfigurationException e) {
        	e.printStackTrace();
        } catch (SAXException e) {
        	e.printStackTrace();
        } finally {
        	
    	}
        
        return nl;
    }
	
	// Method for reverse geocoding
	static public Address getAddress(Context ctx, double lat, double lng) {
		Geocoder gc = new Geocoder(ctx);
		List<Address> addresses = new ArrayList<Address>();
		
		try { addresses = gc.getFromLocation(lat, lng, 2); }
		catch (IOException e) { Log.d("WebService", "Failed to get reverse GeoCode: "+e); }
		
		Log.d("WebService", "Addresses found: "+addresses);
		
		if (addresses.size() > 0) {
			return addresses.get(0);
		} else {
			return null;
		}
	}
}
