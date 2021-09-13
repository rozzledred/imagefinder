package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.net.*;
import java.io.*;

/**
 * The WebScraper class contains functions to recursively traverse a webpage and scrape all images,
 * as well as generate ZIP files.
 */
public class WebScraper {
    private static final int MAX_DEPTH = 0;
    private HashSet<String> links = new HashSet<String>();
    private HashSet<String> images = new HashSet<String>();
    String url;
    String host;
    
    /**
     * Empty constructor for WebScraper class.
     */
    public WebScraper() {
		
	}
    
    /**
     * Constructor for WebScraper class.
     * 
     * @param url  String parameter to specify root URL.
     */
    public WebScraper(String url) throws MalformedURLException {
    	this.url = url;
    	URL urlObject = new URL(url);
    	this.host = urlObject.getHost();
    }
    
    /**
     * Recursive function to traverse webpage with specified depth.
     * 
     * @param URL  String URL to begin with, same as class but parameter is required to recurse.
     * @param depth  Integer parameter to specify how many links deep to traverse recursively.
     */
	public void getPageLinks(String URL, int depth) {
        if ((!links.contains(URL) && depth >= MAX_DEPTH)) {
            System.out.println("|| Depth: " + depth + " (" + URL + ")");
            try {
                links.add(URL);
                Document webpage = Jsoup.connect(URL).get();
                Elements linksOnPage = webpage.select("a[href]");
                Elements imagesOnPage = webpage.getElementsByTag("img");
                Element favicon = webpage.head().select("link[href~=.*\\.(ico|png)]").first();
                if(favicon != null) images.add(favicon.attr("href"));
                for(Element image : imagesOnPage) {
    				String imageUrl = image.absUrl("src");
    				if(images.add(imageUrl)) {
    					System.out.println("Image URL:" + imageUrl);
    				}
    			}
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth-1);
                }
            } 
            catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
            catch (IllegalArgumentException ie) {
				System.out.println("Bad URL:" + URL);
			}
        }
    }
    
	/**
     * Recursive function with traversal of webpages only within the domain. 
     * E.g. google.com with depth 0 will only follow links with google.com as the hostname.
     * 
     * @param URL  String URL to begin with, same as class but parameter is required to recurse.
     */
	public void getPageLinks(String urlToSearch) throws MalformedURLException {
    	URL URLObject = new URL(urlToSearch);
    	String currHost = URLObject.getHost();
 
    	if (!links.contains(urlToSearch) && currHost.equals(host)) {
            System.out.println(">> Depth: " + "[" + urlToSearch + "]");
            try {
                links.add(urlToSearch);
                Document webpage = Jsoup.connect(urlToSearch).get();
                
                Elements linksOnPage = webpage.select("a[href]");
                Elements imagesOnPage = webpage.getElementsByTag("img");
                
                for(Element image : imagesOnPage) {
    				String imageUrl = image.absUrl("src");
    				if(images.add(imageUrl)) {
    					System.out.println("Image URL:" + imageUrl);
    				}
    			}

                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));
                }
            } 
            catch (IOException e) {
                System.err.println("For '" + urlToSearch + "': " + e.getMessage());
            }
            catch (IllegalArgumentException ie) {
				System.out.println("Bad URL:" + urlToSearch);
			}
        }
    }
    
	/**
     * Function used to create ZIP files from scraped images on webpage.
     * 
     * The function uses the images HashSet to write the bytes of the files to a ZipOutputStream,
     * and then converts it to a ByteArrayOutputStream in order to send via the header of the
     * HTTP request.
     * 
     * @throws IOException if malformed URL or error in InputStream.
     * @returns  ByteArrayOutputStream object.
     */
	public ByteArrayOutputStream populateZipOutputStream() throws IOException {
		String[] formats = {"apng", "avif", "gif", "jpg", "jpeg", "png", "svg", "webp"};
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zip = new ZipOutputStream(baos);
    	int i = 0;
    	for(String imageLink : images) {
    		URL url;
			try {
				url = new URL(imageLink);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				continue;
			}
    		InputStream in = url.openStream();
    		System.out.println("Reading from " + url);
    		zip.putNextEntry(new ZipEntry(Integer.toString(++i) + ".png"));
    		int length;
    		byte[] buffer = new byte[2048];
    		while ((length = in.read(buffer)) > -1) {
    		    zip.write(buffer, 0, length);
    		}
    		zip.closeEntry();
    		in.close();
    	}
    	zip.close();
    	baos.close();
    	return baos;
    }
    
    public HashSet<String> getImages(){
    	return images;
    }
}
