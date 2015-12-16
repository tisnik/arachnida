echo "Products:"
sqlite3 ../data.db "select count(*) from products;"

echo "Repos:"
sqlite3 ../data.db "select count(*) from repos;"

echo "Commits:"
sqlite3 ../data.db "select count(*) from commits;"

echo "Branches:"
sqlite3 ../data.db "select count(*) from branches_for_commit;"

echo "Changed files:"
sqlite3 ../data.db "select count(*) from changed_files;"

