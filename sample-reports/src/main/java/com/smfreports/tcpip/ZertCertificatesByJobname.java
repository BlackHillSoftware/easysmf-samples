package com.smfreports.tcpip;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf119.*;
import com.blackhillsoftware.smf.smf119.zert.*;

/**
 * Read zERT detail records, and list certificates used, certificate expiry date
 * and the last time each certificate was used by job name.
 * The certificate information seems to only be available when reported by
 * the crypto provider, not when the zERT record is based on observation.  
 */
public class ZertCertificatesByJobname 
{
	public static void main(String[] args) throws IOException, IllegalArgumentException 
	{
        if (args.length < 1)
        {
            System.out.println("Usage: ZertCertificatesByJobname <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
		
		try (SmfRecordReader reader = SmfRecordReader
				.fromName(args[0])
				.include(119,11)) // zERT detail
		{
			// Group by certificate serial, then by system and jobname
			Map<String, Map<GroupKey, Smf119Record>> result = reader.stream()
				.map(record -> Smf119Record.from(record))
				.filter(r119 -> r119.zertDetailCommonSection().smf119scSaSecProtoTls())
				.filter(r119 -> r119.zertDetailTLSSection() != null)
				// only TLS server type records
				.filter(r119 -> 
					r119.zertDetailTLSSection().smf119scTlsHandshakeRole() == TLSHandshakeRole.SERVER 
					|| r119.zertDetailTLSSection().smf119scTlsHandshakeRole() == TLSHandshakeRole.SRV_CA)
				.collect(Collectors.groupingBy(r119 ->
					// group by certificate serial
					r119.zertDetailTLSSection().smf119scTlsSCertSerial() != null ?
						r119.zertDetailTLSSection().smf119scTlsSCertSerial().asHex()
						: "N/A",
					// GroupKey is system & jobname, collect the SMF record with latest session initiation time for each  
					Collectors.toMap(
						GroupKey::new,
						r119 -> r119,
						BinaryOperator.maxBy(Comparator.comparing(x -> x.zertDetailCommonSection().smf119scSaSDateTime()))
					)
				));
			writeReport(result);
		}
		System.out.println("Finished");
	}

	private static void writeReport(Map<String, Map<GroupKey, Smf119Record>> result) 
	{		
		// column headers
		System.out.format("%-40s %-23s %-8s %-8s %-25s %-8s %-8s %-24s%n",
				"Certificate Serial",
				"Certificate Expiry",
				"Jobname", 
				"System", 
				"Latest", 
				"Jobid", 
				"Userid", 
				"Provider");
		
		result.entrySet().stream()
			// sort by certificate serial
			.sorted(Map.Entry.comparingByKey())
			.forEachOrdered(certEntry -> 
			{
				Map<GroupKey, Smf119Record> jobRecords = certEntry.getValue();
				// certificate information can be from any record
				Smf119Record first = jobRecords.values().iterator().next();
				if (first.zertDetailTLSSection().smf119scTlsSCertSerial() != null)
				{
					// print certificate information
					System.out.format("%n%-40s %s%n", 
							certEntry.getKey(),
							first.zertDetailTLSSection().smf119scTlsSCertTime());
					first.zertDetailCertificateDN().forEach(dn -> 
						System.out.println(" ".repeat(4) + dn.smf119scDn()));
				}
				else
				{
					System.out.println();
					System.out.println("N/A");
				}
				
				// print job information
				jobRecords.entrySet().stream()
					// sort by jobname, system
					.sorted(
						Comparator.comparing((Map.Entry<GroupKey, Smf119Record> entry) -> entry.getKey().jobname)
							.thenComparing(entry -> entry.getKey().system)
					)
					.forEachOrdered(entry -> {
						GroupKey key = entry.getKey();
						Smf119Record r119 = entry.getValue();
						System.out.print(" ".repeat(65));
						System.out.format("%-8s %-8s %-25s %-8s %-8s %-15s%n",
							key.jobname, 
							key.system, 
							r119.zertDetailCommonSection().smf119scSaSDateTime(),
							r119.zertDetailCommonSection().smf119scSaJobID(), 
							r119.zertDetailCommonSection().smf119scSaUserID(),
							r119.zertDetailTLSSection().smf119scTlsProtocolProvider());
					});
			});
	}
	
	/**
	 * Define a java record with the values used to group the SMF records
	 */
	record GroupKey (
			String system, 
			String jobname) 
	{
		public GroupKey(Smf119Record r119)
		{
			this(
				r119.system(), 
				r119.zertDetailCommonSection().smf119scSaJobname());
		}			
	} 
} 

