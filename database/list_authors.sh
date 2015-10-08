sqlite3 ../data.db "select author as author, count(*) as commits_count, sum(files_changed) as files_changed, sum(insertions) as insertions, sum(deletions) as deletions from commits group by author order by author;"

