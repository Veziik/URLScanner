package searchTool;

public class TestHTMLFiles {

	public static StringBuilder TestKatz =  new StringBuilder("<html>"
			+ "<head> <title> Jonathan Katz </title> </head>"
			+ "<frameset cols = \"25%,75%\">"
			+ "<frame src=\"menu.html\" name=\"menu\">" //no slash relative
			+ "<frame src=\"/~jkatz/contact.html\" name=\"content\">" // slash relative
			+ "<frame src=\"../../header.html\" name=\"content\">" // dot dot relative
			+ "</frameset>"
			+ "<!-- Site Meter -->"
			+ "<noscript>"
			+ "<a href=\"https://s-media-cache-ak0.pinimg.com/236x/87/de/bb/87debb51e9144bd56b12c49865d73c3d.jpg\">" //200 img
			+ "<a href=\"http://www.cs.umd.edu/~jkatz/pic.png\">" //404 img
			+ "<a href=\"http://www.cs.umd.edu/~jkatz/giveme404.html\">" //404 html
			+ "<a href=\"https://www.cs.umd.edu/~jkatz/\">" //https site
			+ "<a href=\"http://www.facebook.com/\">" //redirect
			+ "<a href=\"http://www.yahoo.com/\">" //external site
			+ "<a href=\"http://www.yahoo.com/giveme404\">" //external 404
			+ "</noscript>"
			+ "<!-- Copyright (c)2006 Site Meter -->");
	
}
