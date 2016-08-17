SELECT u.* INTO OUTFILE '/tmp/user.txt'
	FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' ESCAPED BY '\\'
	LINES TERMINATED BY '\n'
	FROM
		mysql.user u
	WHERE
		u.Select_priv='Y'
	UNION
	SELECT u.*
	FROM
		mysql.user u,
		mysql.db d
	WHERE
		d.Db IN('hutoma') AND
		d.User = u.user
	UNION
	SELECT u.*
	FROM
		mysql.user u,
		mysql.tables_priv t
	WHERE
		t.Db IN('hutoma') AND
		t.User = u.User
	UNION
	SELECT u.*
	FROM
		mysql.user u,
		mysql.procs_priv p
	WHERE
		p.Db IN('hutoma') AND
		p.User = u.User;
