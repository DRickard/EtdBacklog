UPDATE vger_support.backlog_etds
SET 
	processed = sysdate
WHERE 
        proq_id=&1
/
