package com.smfreports;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.blackhillsoftware.smf.SmfRecord;
import com.blackhillsoftware.smf.SmfRecordReader;
import com.blackhillsoftware.smf.SmfRecordWriter;

public class SmfDeDup
{
	private static void printUsage() {
		System.out.println("Usage: SmfDeDup <input-file> [ output-file [ dup-file ] ]");
		System.out.println("");
		System.out.println("Search for duplicates in input-file, and report duplicate record counts");
		System.out.println("by record type.");
		System.out.println("");
		System.out.println("  input-file   File containing SMF records. Binary data, RECFM=U or V[B]");
		System.out.println("               including RDW.");
		System.out.println("  output-file  Copy data to output-file with duplicates removed.");
		System.out.println("  dup-file     Write duplicate records to dup-file to allow further");
		System.out.println("               investigation. Can only be specified with output-file.");
	}
	
	private static void printWarning() {
		System.out.println("Warning:");
		System.out.println("");
		System.out.println("Some SMF record types seem to naturally include the same data and");
		System.out.println("the SMF record time fields are not granular enough to distinguish");
		System.out.println("them. Duplicate records do not necessarily indicate a problem,");
		System.out.println("they might be valid data.");
		System.out.println("");
	}
	
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException
    {
    	if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h"))
    	{
    		printUsage();
    		System.exit(0);
    	}
    	printWarning();
    	
    	// We don't need a cryptographically secure hash, SHA-1 might be 
    	// faster than SHA-256 hashes and should be adequate to find duplicates
    	MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    	
    	// Hashes will be stored as BigIntegers
    	Set<Digest> recordHashes = new HashSet<>();
    	
    	Map<Integer, RecordStats> duplicatesByType = new HashMap<>(); 
    	
    	// Open reader, and writer and dupwriter classes if provided.
        try (
        	SmfRecordReader reader = SmfRecordReader.fromName(args[0]);                
			SmfRecordWriter writer = args.length > 1 ? SmfRecordWriter.fromName(args[1]) : null;
			SmfRecordWriter dupwriter = args.length > 2 ? SmfRecordWriter.fromName(args[2]) : null;
        	)		
    	{
        	int in = 0;
        	int out = 0;
        	int dups = 0;
            for (SmfRecord record : reader)
            {
            	in++;
                if (recordHashes.add(new Digest(sha1.digest(record.getBytes()))))
                {
                	// new hash, not a duplicate
                	if (writer != null) // if we have an output file for deduplicated records
                	{
                		out++;
                		writer.write(record);
                	}
                }
                else
                {
                	// hash was already in Set i.e. duplicate record
                	dups++;
                	if (dupwriter != null) // if we have an output file for duplicate records
                	{
                		dupwriter.write(record);
                	}
                	duplicatesByType
                		.computeIfAbsent(record.recordType(), key -> new RecordStats(record.recordType()))
                		.count(record);
                }
            }
            System.out.format("Finished, %d records in, %d records out, %d duplicates.%n", in, out, dups);
            
            if (dups > 0)
            {
	            System.out.format("%nDuplicates by type:%n");
	            duplicatesByType.values().stream()
	            	.sorted(Comparator.comparing(RecordStats::getRecordtype))
	            	.forEachOrdered(entry -> System.out.format("%4d : %8d%n", entry.getRecordtype(), entry.getCount()));
            }
    	}

        catch (Exception e)
        {
        	printUsage();
        	throw e;
        }
    }
    
    private static class RecordStats
    {
    	RecordStats(int recordtype)
    	{
    		this.recordtype = recordtype;
    	}
    	
    	private int recordtype;
    	private int count = 0;

        public void count(SmfRecord record)
        {
            count++;
        }

		private int getRecordtype() {
			return recordtype;
		}

		private int getCount() {
			return count;
		}
    }
    
    /**
     * A class to store hashes in the set, providing hashcode and equals implementations 
     */
    private static class Digest
    {
    	public Digest(byte[] digest)
    	{
    		this.digest = digest;
    		// for a SHA-1 hash, the first 4 bytes is probably an adequate 
    		// value to return for hashCode
    		hashCode = ByteBuffer.wrap(digest).getInt();
    	}
    	
    	@Override
		public int hashCode() 
    	{
			return hashCode;
		}
    	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Digest other = (Digest) obj;
			return Arrays.equals(this.digest, other.digest);	
		}
		
		private byte[] digest;
    	private int hashCode;
    	
    }
    
}
