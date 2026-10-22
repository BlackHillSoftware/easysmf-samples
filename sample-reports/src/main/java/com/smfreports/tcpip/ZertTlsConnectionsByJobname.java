package com.smfreports.tcpip;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf119.*;
import com.blackhillsoftware.smf.smf30.*;

/**
 * Read zERT summary records (SMF 119 subtype 12) for client TLS sessions,
 * and list the job, userid, remote server IP, and the first and last
 * summary time for that combination.
 * Programs come from SMF type 30 for the same system, job, userid, and hour.
 */
public class ZertTlsConnectionsByJobname 
{
    private record TlsConn(LocalDateTime time, String system, String jobname, String userid, InetAddress remoteIp) {}
    private record ConnKey(String system, String jobname, String userid, InetAddress remoteIp) {}
    private record JobHour(String system, String jobname, String userid, LocalDateTime hour) {}
    private record ReportRow(String system, String jobname, String userid, LocalDateTime first, LocalDateTime last,
            InetAddress remoteIp, List<String> programs) {}
	
	public static void main(String[] args) throws IOException, IllegalArgumentException 
	{
		List<TlsConn> connections = new ArrayList<>();
		Map<JobHour, Set<String>> jobPrograms = new HashMap<>();
		
        if (args.length < 1)
        {
            System.out.println("Usage: ZertTlsConnectionsByJobname <input-name>");
            System.out.println("<input-name> can be filename, //DD:DDNAME or //'DATASET.NAME'");          
            return;
        }
		
		try (SmfRecordReader reader = SmfRecordReader
				.fromName(args[0])
				.include(119,12) // zERT summary
				.include(30)) 
		{
			for (var record : reader)
			{
				switch (record.recordType())
				{
				case 119:
					var r119 = Smf119Record.from(record);
					if (r119.zertSummaryCommonSection().smf119ssSaClient() 
							&& r119.zertSummaryCommonSection().smf119ssSaSecProtoTls())
					{
						connections.add(new TlsConn(r119.smfDateTime(),
								r119.system(),
								r119.zertSummaryCommonSection().smf119ssSaJobname(),
								r119.zertSummaryCommonSection().smf119ssSaUserID(),
								r119.zertSummaryCommonSection().smf119ssSaSrvIp()));
					}
					break;
				case 30:
					// Subtypes 2-3 are interval records. Subtype 4 is the step total
					// and can cover many hours. Subtype 5 job-end program information
					// is not useful here.
					
					if (record.subType() >= 2 && record.subType() <= 4)
					{
						var r30 = Smf30Record.from(record);
						var processors = r30.processorAccountingSection();
						if (processors == null)
						{
							break;
						}
						String program = r30.identificationSection().smf30exn() != null && r30.identificationSection().smf30exn().length() > 0 ?
								r30.identificationSection().smf30exn()
								: r30.identificationSection().smf30pgm();
						if (program != null && program.length() > 0)
						{
							String system = r30.system();
							String jobname = r30.identificationSection().smf30jbn();
							String userid = r30.identificationSection().smf30rud();
							LocalDateTime recordHour = r30.smfDateTime().truncatedTo(ChronoUnit.HOURS);
							if (record.subType() == 4)
							{
								LocalDateTime hour = r30.identificationSection().smf30std()
										.atTime(r30.identificationSection().smf30sit())
										.truncatedTo(ChronoUnit.HOURS);
								while (!hour.isAfter(recordHour))
								{
									addProgram(jobPrograms, system, jobname, userid, hour, program);
									hour = hour.plusHours(1);
								}
							}
							else
							{
								addProgram(jobPrograms, system, jobname, userid, recordHour, program);
								LocalDateTime intervalStart = processors.smf30idt()
										.atTime(processors.smf30ist())
										.truncatedTo(ChronoUnit.HOURS);
								if (!intervalStart.equals(recordHour))
								{
									addProgram(jobPrograms, system, jobname, userid, intervalStart, program);
								}
							}
						}
					}
					break;
				default:
					break;
				}
			}
			
			writeReport(connections, jobPrograms);
		}
		System.out.println("Finished");
	}

	private static void writeReport(List<TlsConn> connections, Map<JobHour, Set<String>> jobPrograms) 
	{
		Map<ConnKey, List<TlsConn>> grouped = new HashMap<>();
		for (TlsConn conn : connections)
		{
			grouped.computeIfAbsent(
					new ConnKey(conn.system(), conn.jobname(), conn.userid(), conn.remoteIp()),
					key -> new ArrayList<>())
				.add(conn);
		}

		List<ReportRow> rows = new ArrayList<>();
		for (var entry : grouped.entrySet())
		{
			LocalDateTime first = null;
			LocalDateTime last = null;
			Set<String> programs = new TreeSet<>();
			for (TlsConn conn : entry.getValue())
			{
				if (first == null || conn.time().isBefore(first))
				{
					first = conn.time();
				}
				if (last == null || conn.time().isAfter(last))
				{
					last = conn.time();
				}
				programs.addAll(jobPrograms.getOrDefault(new JobHour(
						conn.system(),
						conn.jobname(),
						conn.userid(),
						conn.time().truncatedTo(ChronoUnit.HOURS)),
						Set.of()));
			}
			ConnKey key = entry.getKey();
			rows.add(new ReportRow(key.system(), key.jobname(), key.userid(), first, last,
					key.remoteIp(), List.copyOf(programs)));
		}
		rows.sort(Comparator.comparing(ReportRow::jobname)
				.thenComparing(ReportRow::system)
				.thenComparing(ReportRow::userid)
				.thenComparing(ReportRow::first));

		System.out.format("%-8s %-8s %-8s %-24s %-24s %-40s%n",
				"Job",
				"System",
				"Userid",
				"First",
				"Last",
				"Remote IP"
				);
		for (ReportRow row : rows)
		{
			System.out.format("%-8s %-8s %-8s %-24s %-24s %-40s%n", 
					row.jobname(),
					row.system(),
					row.userid(), 
					row.first(),
					row.last(),
					row.remoteIp().getHostAddress());
			writePrograms(row.programs());
		}
	}

	private static void addProgram(Map<JobHour, Set<String>> jobPrograms,
			String system, String jobname, String userid, LocalDateTime hour, String program)
	{
		jobPrograms.computeIfAbsent(new JobHour(system, jobname, userid, hour),
				job -> new HashSet<>())
			.add(program);
	}

	private static void writePrograms(List<String> programs)
	{
		if (programs.isEmpty())
		{
			return;
		}

		final int perLine = 4;
		// Job, system, and userid columns, so names start under First.
		final String indent = " ".repeat(8 + 1 + 8 + 1 + 8 + 1);
		for (int i = 0; i < programs.size(); i++)
		{
			if (i % perLine == 0)
			{
				if (i > 0)
				{
					System.out.println();
				}
				System.out.print(indent);
			}
			else
			{
				System.out.print(' ');
			}
			System.out.format("%-8s", programs.get(i));
		}
		System.out.println();
		System.out.println();
	}
} 

