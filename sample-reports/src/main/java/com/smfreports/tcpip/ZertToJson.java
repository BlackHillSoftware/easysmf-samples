package com.smfreports.tcpip;

import java.io.*;

import com.blackhillsoftware.json.EasySmfGsonBuilder;
import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf119.*;
import com.google.gson.Gson;

public class ZertToJson 
{
	public static void main(String[] args) throws IOException, IllegalArgumentException 
	{
        if (args.length < 1)
        {
            System.out.println("Usage: ZertToJson <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
		
        Gson gson = customizeGson();
        
		try (SmfRecordReader reader = SmfRecordReader
				.fromName(args[0])
				.include(119,11) // zERT detail
				//.include(119,12) // zERT summary
				)
		{
			reader.stream()
				.map(record -> Smf119Record.from(record))
                .forEach(r119 -> 
                System.out.println(gson.toJson(r119))
                );     
		}
		System.out.println("Finished");
	}

	private static Gson customizeGson() 
	{
		// Setup Gson to generate JSON
        return new EasySmfGsonBuilder()
                .setPrettyPrinting()       // pretty printing = human readable
                .includeUnsetFlags(false)
                
                // exclude various fields that are not useful
                .exclude("selfDefiningSection")

                .exclude("hasSubtypes")
                .exclude("recordLength")
                .exclude("recordType")
                .exclude("smfDate") // use combined date/time
                .exclude("smfTime")
                .exclude("subSystem")
                .exclude("subType")
                .exclude("smf119hdFlags") // raw value is not useful
                .exclude("smf119hdLength")
                .exclude("smf119hdSegDesc")
                .exclude("smf119hdSid")
                .exclude("smf119hdSp2")
                .exclude("smf119hdSp3")
                .exclude("smf119hdSp4")
                .exclude("smf119hdSsi")
                .exclude("smf119hdDate")
                .exclude("smf119hdTime")
                .exclude("smf119hdType")
                .exclude("smf119hdVs2")
                .exclude("smf119hdsub")               
                .exclude("smf119ssSaFlags") 
                
                .exclude("smf119scSaFlags") 
                .exclude("smf119scSaEDate") 
                .exclude("smf119scSaETime") 
                .exclude("smf119scSaSDate") 
                .exclude("smf119scSaSTime") 
                .exclude("smf119scSaSecFlags") 
                .exclude("smf119scSaSecProtos") // flags

                
                .exclude("smf119scTlsSessionIdLen") 

                .exclude("smf119scTlsSCertSerialLen") 
                .exclude("smf119scTlsSCertTimeType")
                
                // exclude Token entries and create calculated entry with hex value
                .exclude("smf119scTlsSessionId") 
                .calculateEntry(ZertDetailTLSSection.class, "smf119scTlsSessionId", 
                		tlsSection -> tlsSection.smf119scTlsSessionId() != null ? 
                				tlsSection.smf119scTlsSessionId().asHex() 
                				: null)
                .exclude("smf119scTlsSCertSerial") 
                .calculateEntry(ZertDetailTLSSection.class, "smf119scTlsSCertSerial", 
                		tlsSection -> tlsSection.smf119scTlsSCertSerial() != null ? 
                				tlsSection.smf119scTlsSCertSerial().asHex() 
                				: null)

                .exclude("smf119scTlsCCertSerialLen") 
                .exclude("smf119scTlsCCertTimeType")
                // exclude Token entries and create calculated entry with hex value
                .exclude("smf119scTlsCCertSerial")
                .calculateEntry(ZertDetailTLSSection.class, "smf119scTlsCCertSerial", 
                		tlsSection -> tlsSection.smf119scTlsCCertSerial() != null ? 
                				tlsSection.smf119scTlsCCertSerial().asHex() 
                				: null)       
                
                .exclude("smf119scDnLen")
                .createGson();
	}
}
