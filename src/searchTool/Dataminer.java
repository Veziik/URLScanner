package searchTool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Dataminer {

	public ArrayList<Webpage> pages = null;
	public HashSet<Webpage> container = null;
	public HashMap<Integer, HashSet<Webpage>>  mapStatusToURL = new HashMap<Integer, HashSet<Webpage>>();
	public HashMap<Webpage ,String> problemsByPatterns = new HashMap<Webpage , String>();
	public HashMap<String,HashSet<Webpage>> mapFileTypeToURL = new HashMap<String,HashSet<Webpage>>();
	
public Dataminer( String newPath){
}

public Dataminer(){
	pages = new ArrayList<Webpage>();
	container = new HashSet<Webpage>();
}

public String traceRedirectionsToWebpage( Webpage parent, Webpage child){
	String allRedirections = ")";
	ArrayList<Webpage> all300s = find300To399();
	Webpage checkPage = child;
	
	while( !allRedirections.contains(checkPage.url.toString())){
		 allRedirections = allRedirections.startsWith(")") ? checkPage.url + allRedirections : checkPage.url + " --> " + allRedirections;
		 
		 for(Webpage page : all300s){
			 if(parent != null && parent.children.contains(page) && page.redirect.equals(checkPage)){
				 checkPage = page;
				 break;
			 }
		 }
	}	
	
	return "(" + parent.url + " --> "+ allRedirections;
}

public String findWebpageOnParentThatRedirectsToChild( Webpage parent, Webpage child){
	String allRedirections = ")";
	ArrayList<Webpage> all300s = find300To399();
	Webpage checkPage = child;
	
	while( !allRedirections.contains(checkPage.url.toString())){
		 allRedirections = allRedirections.startsWith(")") ? checkPage.url + allRedirections : checkPage.url + " --> " + allRedirections;
		 
		 for(Webpage page : all300s){
			 if(parent != null && parent.children.contains(page) && page.redirect.equals(checkPage)){
				 checkPage = page;
				 break;
			 }
		 }
	}	
	
	return checkPage.url.toString();
}

public void summarizeWebpagePopulations(){	
	
	for(Integer status : mapStatusToURL.keySet()){
		MyDesignTimePatterns.appendToTextFile(status + " = " + mapFileTypeToURL.get(status).size(), MyDesignTimePatterns.statusesFile);
	}
	
	for(String fileType : mapFileTypeToURL.keySet()){
		MyDesignTimePatterns.appendToTextFile(fileType + " = " + mapFileTypeToURL.get(fileType).size(), MyDesignTimePatterns.typeFile);
		
	}
	
}

public ArrayList<Webpage> findErrors(){
	ArrayList<Webpage> all0s = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.toString().contains("Status: 0"))
			all0s.add(page);
	
	return all0s;
}

public ArrayList<Webpage> find200(){
	ArrayList<Webpage> all200s = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.toString().contains("Status: 200"))
			all200s.add(page);
	
	return all200s;
}

public ArrayList<Webpage> find300To399(){
	ArrayList<Webpage> all300Plus = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.toString().contains("Status: 3"))
			all300Plus.add(page);
	
	return all300Plus;
}

public ArrayList<Webpage> find400To499(){
	ArrayList<Webpage> all400Plus = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.toString().contains("Status: 4") && !page.toString().contains("Status: 404"))
			all400Plus.add(page);
	
	return all400Plus;
}

public ArrayList<Webpage> findNot200(){
	ArrayList<Webpage> not200s = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(!page.toString().contains("Status: 200") && !page.toString().contains("Status: 0"))
			not200s.add(page);
	
	return not200s;
}


public ArrayList<Webpage> findDotDot(){
	ArrayList<Webpage> dotDots = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.isRelative && page.relativeURL.contains("../"))
			dotDots.add(page);
	
	return dotDots;
}

public ArrayList<Webpage> findAll404(){
	ArrayList<Webpage> all404s = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
			
		if(page.status == 404 && !(page.isRelative && page.relativeURL.contains("../")))
			all404s.add(page); 
	
	return all404s;
}

public ArrayList<Webpage> findRelative404(){
	ArrayList<Webpage> all404s = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.toString().contains("Status: 404") && page.isRelative && !page.relativeURL.startsWith("."))
			all404s.add(page);
	
	return all404s;
}

public ArrayList<Webpage> findAbsolute404(){
	ArrayList<Webpage> all404s = new ArrayList<Webpage>();
	
	for(Webpage page : pages)
		if(page.toString().contains("Status: 404") && !page.isRelative)
			all404s.add(page);
	
	return all404s;
}

public void writeAllFormats( String filename, ArrayList<Webpage> items){	 	
	write(filename, items);
	writeSimple(filename, items);
	writeParentsFirst(filename,items);
}

public void write(String filename, ArrayList<Webpage> items){
	PrintWriter writer;
	int outerCount = 0, innerCount;
				
		try {
			if(!items.isEmpty()){
				
				writer = new PrintWriter(filename +  "WithRedirectionTrace.csv", "UTF-8");
				MyDesignTimePatterns.appendToTextFile("writing " + filename + filename.substring(filename.lastIndexOf("\\"))+  "WithRedirectionTrace.csv", MyDesignTimePatterns.progressTextFile);
				writer.println(filename.substring(filename.lastIndexOf("\\") + 1)+"WithLocationReferred");
				
				writer.println("\n" + items.size() + " total webpages.");
				
					for(int i = 0; i < items.size(); i++){
						writer.print(++outerCount + " , " + items.get(i).url + " , " + pages.get(i).IPAddress + " , " + items.get(i).status);
						if(items.get(i).isRelative){
							writer.print(" , Possible Relative URL on Parent: " + items.get(i).relativeURL );
						}
						writer.println();
						
						innerCount = 1;
						for(Webpage page: pages)
							if(page.children.contains(items.get(i)))
								writer.println(outerCount + "." + innerCount++ + " , [intentionally left blank] , " + page.url + " , Redirection trace from page: " + traceRedirectionsToWebpage(page, items.get(i))); 	
						
						writer.println(",,,");
					}
				
				
				writer.close();
			}else
				System.out.println("Nothing to print for " + filename);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		
	
	
}

public void writeWithoutTrace(String filename, ArrayList<Webpage> items){
	PrintWriter writer;
	int outerCount = 0, innerCount;
				
		try {
			if(!items.isEmpty()){
				
				writer = new PrintWriter(filename +  "WithLocationReferred.csv", "UTF-8");
				MyDesignTimePatterns.appendToTextFile("writing " + filename + filename.substring(filename.lastIndexOf("\\")) + "WithLocationReferred.csv", MyDesignTimePatterns.progressTextFile);
				writer.println(filename.substring(filename.lastIndexOf("\\") + 1)+"WithLocationReferred");
				
				writer.println("\n" + items.size() + " total webpages.");
				
					for(int i = 0; i < items.size(); i++){
						writer.print(++outerCount +" , " + items.get(i).url+" , " + pages.get(i).IPAddress + " , " + items.get(i).status);
						if(items.get(i).isRelative){
							writer.print(" , Possible Relative URL on Parent: " + items.get(i).relativeURL );
						}
							writer.println();
						
						innerCount = 1;
						for(Webpage page: pages)
							if(page.children.contains(items.get(i))){
								writer.println(outerCount + "." + innerCount++ + " , [intentionally left blank] , " + page.url + " , URL to find on page: " + findWebpageOnParentThatRedirectsToChild(page, items.get(i))); 				
									
							}
						writer.println(",,,");
					}
				
				
				writer.close();
			}else
				System.out.println("Nothing to print for " + filename);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		
	
	
}

public void writeParentsFirst(String filename, ArrayList<Webpage> items){
	PrintWriter writer;	
	int outerCount = 0, innerCount;
	boolean isParentInDirectory = false;
	 
		try {
			if(!items.isEmpty()){
				
				writer = new PrintWriter(filename + "ByParentPage.csv", "UTF-8");
				MyDesignTimePatterns.appendToTextFile("writing " + filename + filename.substring(filename.lastIndexOf("\\")) + "ByParentPage.csv", MyDesignTimePatterns.progressTextFile);
				writer.println(filename.substring(filename.lastIndexOf("\\") + 1)+"ByPage");
				writer.print( "Allowed pages:");
				
					
				writer.println("\n" + items.size() + " total webpages.");
				
					for(int i = 0; i < pages.size(); i++){
						
						for(Webpage page: items)
							if(pages.get(i).children.contains(page)){
								isParentInDirectory = true;		
								break;
							}
						
						if(isParentInDirectory){
								
							writer.println( "\n" + ++outerCount + " , " + pages.get(i).url + " , " + pages.get(i).IPAddress + " , "+ pages.get(i).status);
							
							innerCount = 1;
							for(Webpage page: pages.get(i).children)
								if(items.contains(page)){
										writer.println(outerCount + "." + innerCount++ + " , Link on Parent: "  + findWebpageOnParentThatRedirectsToChild(pages.get(i), page) + " , " +  page.url.toString()+ " , " + pages.get(i).IPAddress  + " , " + page.status);
								}							
						}
						isParentInDirectory = false;
					}
				
				writer.close();
			}else
				System.out.println("Nothing to print for " + filename);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}		
	

}

public void writeSimple(String filename, ArrayList<Webpage> items){
	PrintWriter writer;	
			
		try {
			if(!items.isEmpty()){
				
				writer = new PrintWriter(filename + "SimpleFormat.csv", "UTF-8");
				MyDesignTimePatterns.appendToTextFile("writing " + filename + filename.substring(filename.lastIndexOf("\\")) + "SimpleFormat.csv", MyDesignTimePatterns.progressTextFile);
				writer.println("Simple Format");
				writer.println( "Allowed pages:, " + MyDesignTimePatterns.AllowedDomainNames);
				
					
				writer.println("\n" + items.size() + " total webpages.\n");
				writer.println("URL on Parent, Actual URL, Status, IP address, parent");
				
					for(Webpage page : items){
						if(page.parent != null){
							writer.print( "\n"+  findWebpageOnParentThatRedirectsToChild(page.parent, page) + " , " + page.url + " , " + page.status + " , "+ page.IPAddress + " , " + page.parent.url);
						}else{
							writer.print( "\n [Root Link] , " + page.url + " , " + page.status + " , " + page.IPAddress + " , [No Parent]" );
						}		
					}
					
				writer.close();
			}else
				System.out.println("Nothing to print for " + filename);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
	

}

public String writeAsHTML( String path, ArrayList<Webpage> items){
	PrintWriter writer;
	
			
		try {
			if(!items.isEmpty()){				
				writer = new PrintWriter( path + "\\" + path.substring(path.lastIndexOf("\\") + 1, path.length()) + ".html", "UTF-8");
				
				writer.println("<!DOCTYPE html>");
				writer.println("<html lang=\"en\">");
				writer.println("<head><meta charset=\"utf-8\"/></head>");
				writer.println("<body><h1>" + path.substring(path.lastIndexOf("\\") + 1, path.length()) + "</h1>");
				writer.println("<p>Allowed pages:");
								
				writer.println("</p>" + items.size() + " total webpages.");
				
				
					writer.print("<table>");
				
					for(int i = 0; i < items.size(); i++){
						writer.print("</br>" + items.get(i).depth+"</br>");
						writer.print("<strong>Child Site:</strong><a href=\""+items.get(i).url+"\"> ");
						writer.print(items.get(i).url + "</a></br>");
						writer.print("<strong>Parent sites:</strong>");
						
						for(Webpage page: pages)
							if(page.children.contains(items.get(i))){
								writer.print(" <a href=\""+page.url+"\"> ");
								writer.print(page.url + "</a>");
								
							}
						
						writer.print("</br>");
						
						if(i < items.size() - 1)
							writer.println("</br>");
					
					}
				
					writer.println("</table>\n</body>\n</html>");
				
				
				writer.close();
			}else
				System.out.println("Nothing to print for " + path.substring(path.lastIndexOf("\\") + 1, path.length()));
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		
	
	return path;
}



}
