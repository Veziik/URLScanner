package searchTool;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Webpage {

	public Webpage parent = null, 
			redirect = null, 
			redirectParent = null;
	public int status = -1;
	public String contentType = null, 
			depth = null, 
			relativeURL = null, 
			IPAddress = null;
	public ArrayList<Webpage> children = new ArrayList<Webpage>();
	public URL url = null;
	public boolean isRelative = false;
	
	public Webpage(String newURL) throws IOException{
		url = new URL(newURL);		
	}
	
	@Override
	public String toString() {
		String parentString, relative = null, redirectString = null;
		ArrayList<String> childrenNames = new ArrayList<String>();
		
		for(Webpage page : children)
			childrenNames.add(page.url.toString());
		
		relative = isRelative ? " , Relative URL: " + relativeURL : "";
		redirectString = status >= 300 && status < 400 ? " , Redirects To: " + redirect: "";
		parentString = parent == null ? " , no Parent" : " , " + parent.url.toString();
		
			return	IPAddress + " , " + url + relative + redirectString + parentString;
	} 
	
	
	public int hashcode(){
		int hash = 1;
		String urlString = url.toString();
		
		for(int i = 0; i < urlString.length() ;i++)
			hash = (hash*31) + urlString.charAt(i);
		
		return hash;
	}
	
	
	@Override
	public boolean equals(Object o){
		
		if(o == this)
			return true;
		
		if(!(o instanceof Webpage))
			return false;
		
		Webpage comp = (Webpage)o;
		
		return hashcode() == comp.hashcode();
	}
}
