package com.smfreports.dataset;
import java.time.LocalDateTime;

import com.blackhillsoftware.smf.*;
import com.blackhillsoftware.smf.smf14.Smf14Record;
import com.blackhillsoftware.smf.smf17.Smf17Record;
import com.blackhillsoftware.smf.smf18.Smf18Record;
import com.blackhillsoftware.smf.smf61.Smf61Record;
import com.blackhillsoftware.smf.smf62.Smf62Record;
import com.blackhillsoftware.smf.smf64.Smf64Record;
import com.blackhillsoftware.smf.smf65.Smf65Record;

/**
 * Class used by DatasetActivity report to collect dataset activity events. 
 *
 */
public class DatasetActivityEvent 
    {
        private LocalDateTime time;
        private String datasetname;
        private String jobname;
        private String userid;
        private String event;
        private String newname;
        private boolean readEvent = false;
        
        // Force creation of DatasetActivityEvent using the from(SmfRecord) method
        private DatasetActivityEvent()
        {
        }
                
        /**
         * 
         * @param record a SMF record of one of the types understood here 
         * @return DatasetActivityEvent representing the dataset activity
         */
        public static DatasetActivityEvent from(SmfRecord record) 
        {    
            switch (record.recordType())
            {
            // 14 and 15 use the same mapping
            case 14: // Read
            case 15: // Update
                return DatasetActivityEvent.from(Smf14Record.from(record));
            case 17: // scratch
                return DatasetActivityEvent.from(Smf17Record.from(record));
            case 18: // rename
                return DatasetActivityEvent.from(Smf18Record.from(record));
            case 61: // ICF define
                return DatasetActivityEvent.from(Smf61Record.from(record));
            case 62: // VSAM open
                return DatasetActivityEvent.from(Smf62Record.from(record));
            case 64: // VSAM Status
                return DatasetActivityEvent.from(Smf64Record.from(record));
            case 65: // ICF delete
                return DatasetActivityEvent.from(Smf65Record.from(record));
            }
            throw new IllegalArgumentException("Unexpected record type: " + record.recordType());
        }
        
        /**
         * Create a DatasetActivityEvent from a type 14 (read) or type 15 (update) record
         * @param r14 a type 14 or 15 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf14Record r14)
        {
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r14.smfjfcb1().jfcbdsnm();
            datasetevent.jobname = r14.smf14jbn();
            datasetevent.userid = r14.smf14uid();
            datasetevent.time = r14.smfDateTime();
            if (r14.recordType() == 15)
            {
                datasetevent.event = "Update (15)";
                
            }
            else
            {
                datasetevent.event = "Read (14)";
                datasetevent.readEvent = true;
            }

            return datasetevent;
        }
        
        /**
         * Create a DatasetActivityEvent from a type 17 (scratch) record
         * @param r17 a type 17 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf17Record r17)
        {
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r17.smf17dsn();
            datasetevent.jobname = r17.smf17jbn();
            datasetevent.userid = r17.smf17uid();
            datasetevent.time = r17.smfDateTime();
            datasetevent.event = "Delete (17)";
            return datasetevent;
        }
        
        /**
         * Create a DatasetActivityEvent from a type 18 (rename) record
         * @param r18 a type 18 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf18Record r18)
        {                    
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r18.smf18ods();
            datasetevent.jobname = r18.smf18jbn();
            datasetevent.userid = r18.smf18uid();
            datasetevent.time = r18.smfDateTime();
            datasetevent.event =  "Rename (18)";
            datasetevent.newname = r18.smf18nds();
            return datasetevent;

        }
        
        /**
         * Create a DatasetActivityEvent from a type 61 (ICF Define) record
         * @param r61 a type 61 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf61Record r61)
        {
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r61.smf61enm();
            datasetevent.jobname = r61.smf61jnm();
            datasetevent.userid = r61.smf61uid();
            datasetevent.time = r61.smfDateTime();
            datasetevent.event = "Create (61)";
            return datasetevent;
        }
        
        /**
         * Create a DatasetActivityEvent from a type 62 (VSAM Open) record
         * @param r62 a type 62 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf62Record r62)
        {
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r62.smf62dnm();
            datasetevent.jobname = r62.smf62jbn();
            datasetevent.userid = r62.smf62uif();
            datasetevent.time = r62.smfDateTime();
            if ((r62.statisticsSection().smf62mc1() & 0x02) != 0)
            {
                datasetevent.event = "Update (62)";
                
            }
            else
            {
                datasetevent.event = "Read (62)";
                datasetevent.readEvent = true;
            }
            return datasetevent;
        }
        
        /**
         * Create a DatasetActivityEvent from a type 64 (VSAM Status) record
         * @param r64 a type 64 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf64Record r64)
        {
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r64.smf64dnm();
            datasetevent.jobname = r64.smf64jbn();
            datasetevent.userid = r64.smf64uif();
            datasetevent.time = r64.smfDateTime();
            if ((r64.statisticsSection().smf64mc1() & 0x02) != 0)
            {
                datasetevent.event = "Update (64)";                
            }
            else
            {
                datasetevent.event = "Read (64)";
                datasetevent.readEvent = true;
            }
            return datasetevent;
        }
        
        /**
         * Create a DatasetActivityEvent from a type 65 (ICF Delete) record
         * @param r65 a type 65 SMF record.
         * @return the DatasetActivityEvent
         */
        private static DatasetActivityEvent from(Smf65Record r65)
        {
            DatasetActivityEvent datasetevent = new DatasetActivityEvent();
            datasetevent.datasetname = r65.smf65enm();
            datasetevent.jobname = r65.smf65jnm();
            datasetevent.userid = r65.smf65uid();
            datasetevent.time = r65.smfDateTime();
            datasetevent.event = "Delete (65)";
            return datasetevent;
        }

        public String getDatasetname() {
            return datasetname;
        }
        
        public String getNewname() 
        {
            // return blank instead of null otherwise "null" prints in report
            return newname != null ? newname : "";
        }

        public String getJobname() {
            return jobname;
        }
        
        public String getUserid() {
            return userid;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public String getEvent() {
            return event;
        }
        
        public boolean isRead() {
            return readEvent;
        }        
    }