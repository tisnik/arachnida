echo "Products:"
sqlite3 ../data.db "select * from products;"

echo "Repos:"
sqlite3 ../data.db "select * from repos;"

echo "Commits:"
sqlite3 ../data.db "select * from commits;"

echo "Branches:"
sqlite3 ../data.db "select * from branches_for_commit;"

echo "Changed files:"
sqlite3 ../data.db "select * from changed_files;"

