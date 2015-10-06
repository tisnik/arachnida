lastdir=`pwd`
cd $1
git checkout $2
git log --pretty=oneline --no-abbrev --shortstat --no-merges > $lastdir/stats

