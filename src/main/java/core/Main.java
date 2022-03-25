package core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.List;

import org.json.JSONObject;

public class Main {

	public static void main(String[] args) {
		
		String url = "https://maps.googleapis.com/maps/api/elevation/json";
		
		String response = get(url, 
			new Param("key","AIzaSyDp_PLQ8mFyXlVkjw_JxKgMcchqrJlCspk")
		);
		
		JSONObject obj = new JSONObject(response);
		
		System.out.println(obj.getString("next"));
		

	}
	
	private static String get(String url, Param... params) {
		
		//Construct URL
		boolean parameterised = false;
		for(Param param : params) {
			url += parameterised ? "&" : "?";
			parameterised = true;
			url += param.name + "=" + param.value;	
		}
		
		//Query API
		try {
            Builder builder = HttpRequest.newBuilder().uri( URI.create( url.toString() ) );
            HttpRequest request = builder.method("GET", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.body();
            
        } catch (IOException e) {
            e.printStackTrace();
            return "Error";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Error";
        }
	
	}
	
	static class Param {
		public final String name, value;
		private Param(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

}
