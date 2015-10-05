lastdir=`pwd`
cd $1
git checkout $2
git log --oneline --no-abbrev-commit --shortstat --no-merges > $lastdir/stats

