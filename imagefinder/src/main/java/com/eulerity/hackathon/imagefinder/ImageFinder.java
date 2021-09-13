package com.eulerity.hackathon.imagefinder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document;  
import org.jsoup.nodes.Element;  
import org.jsoup.select.Elements; 

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  };
	
	  /**
	   * This method overrides the POST request handler, and returns either a JSON or a ZIP file as a HTTP Response.
	   * 
	   * The method checks the incoming request query for certain parameters specifying either Download or
	   * Display functionality, depending on which it responds to the incoming request with a parsed JSON
	   * of image URLs, or a generated ZIP file with all scraped images after crawling the specified URL
	   * recursively.
	   * 
	   * @param req  HTTPServletRequest incoming request parameter.
	   * @param resp  HTTPServletRequest outgoing response.
	   */
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getServletPath();
		String url = req.getParameter("url");
		int depth = Integer.parseInt(req.getParameter("depth"));
		String download = req.getParameter("download");
		System.out.println("Download: " + download);
		resp.setContentType("text/json");
		System.out.println("Depth: " + depth);
		WebScraper crawler = new WebScraper(url);
		
		if(depth == 0) {
			crawler.getPageLinks(url);
		}
		else crawler.getPageLinks(url, depth);
		
		System.out.println("DONE CRAWLING");
		HashSet<String> crawlerImages = crawler.getImages();
		if(download.equalsIgnoreCase("true")) {
			resp.setContentType("application/zip");
		    resp.addHeader("Content-Disposition","attachment; filename=" + crawler.host + ".zip");
	        resp.setHeader("Set-Cookie", "fileDownload=true; path=/");
		    ByteArrayOutputStream baos = crawler.populateZipOutputStream();
		    byte[] fileByteArray = baos.toByteArray();
	        resp.setContentLength(fileByteArray.length);
	        OutputStream responseOutputStream = resp.getOutputStream();
	        responseOutputStream.write(baos.toByteArray());
	        responseOutputStream.close();
		}
		else {
			String finalJson = crawlerImages.isEmpty() ? GSON.toJson(testImages) : GSON.toJson(crawlerImages);
			resp.getWriter().print(finalJson);
		}
	}
}
