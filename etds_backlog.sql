-- other settings via vger_sqlplus_run
SET linesize 200;

SELECT 
        record_id || ':' ||
        proq_id
FROM 
        vger_support.backlog_etds 
WHERE 
	embargo_code=&1
        AND processed IS NULL
        AND proq_id NOT IN (SELECT proq_id FROM vger_support.processed_etds)
/
