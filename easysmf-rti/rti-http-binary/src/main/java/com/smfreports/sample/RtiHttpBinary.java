package com.smfreports.sample;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;

import com.blackhillsoftware.smf.realtime.*;

/**
 * Read data from a SMF in memory resource and send the 
 * complete SMF records in binary format over HTTP.
 */
public class RtiHttpBinary
{
	public static void main(String[] args) 
	        throws IOException, InterruptedException, URISyntaxException 
	{
        if (args.length < 2)
        {
            System.out.println("Usage: RtiHttpBinary <resource-name> <url>");
            return;
        }
        
        String inMemoryResource = args[0];
        String url = args[1];
        
        // Create a HttpClient and request builder (HttpClient requires Java 11) 
        HttpClient client = 
            HttpClient.newBuilder()
                .build();    
        Builder requestBuilder = 
            HttpRequest.newBuilder(new URI(url))
                .header("Content-Type", "application/octet-stream");
        
        // Create the connection to the in memory resource,
        // to be closed when a MVS STOP command is received.
		try (SmfConnection connection = 
				SmfConnection
					.forResourceName(inMemoryResource) 
					.disconnectOnStop()
					.onMissedData(RtiHttpBinary::handleMissedData)
					.connect())
		{
		    // We can send multiple SMF records in a single post, concatenated 
		    // in a byte array.
		    // We queue them in a ByteArrayOutputStream, and send them when 
		    // the SmfConnection tells us there are no records queued in the 
		    // connection i.e. the next read is likely to wait, or if the 
		    // total queued bytes reaches a threshold
		    
		    ByteArrayOutputStream outputQueue = new ByteArrayOutputStream();

			for (byte[] record : connection) // read next SMF record
			{
				outputQueue.write(record); // add it to our queue
				// if nothing more currently available, or 10MB queued
				if (!connection.moreQueued()
						|| outputQueue.size() > 10 * 1024 * 1024)   
				{
				    // send data and create a new queue 
					sendData(client, requestBuilder, outputQueue.toByteArray());             
                    outputQueue= new ByteArrayOutputStream();
				}
			}
			// end of input, most likely due to MVS STOP command.
			// send any remaining data
			if (outputQueue.size() > 0) 
			{
                sendData(client, requestBuilder, outputQueue.toByteArray());             			    
			}
		} 	
	}

	/**
	 * POST the SMF data to a HTTP server. 
	 * 
	 * @param client a HttpClient
	 * @param requestBuilder a HttpRequestBuilder
	 * @param payload the byte array containing 1 or more complete SMF records
	 * @throws IOException if the HttpClient send throws an IOException
	 * @throws InterruptedException if the HttpClient send is interrupted
	 */
    private static void sendData(HttpClient client, Builder requestBuilder, byte[] payload)
            throws IOException, InterruptedException 
    {
        // create the HttpRequest and POST the records
        HttpRequest request = 
            requestBuilder
                .POST(BodyPublishers.ofByteArray(payload))
                .build();					
        HttpResponse<String> response =
            client.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200)
        {
            System.out.println(response.statusCode() + " " + response.body());
        }
    }

    /**
     * Process the missed data event. This method prints a message
     * and indicates that an exception should not be thrown.
     * @param e the missed data event information
     */
	static void handleMissedData(MissedDataEvent e)
	{
		System.out.println("Missed Data!");
		e.throwException(false);	
	}
}
