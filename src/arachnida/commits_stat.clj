(ns arachnida.commits-stat)

(require '[clj-jgit.porcelain :as jgit])
(require '[clj-jgit.querying  :as jgit-query])
(require '[clj-jgit.internal  :as jgit-internal])

(require '[arachnida.exec     :as exec])

(defn parse-sha
    [input-text]
    (re-find #"[a-zA-Z0-9]+" input-text))

(defn parse-files-changed
    [input-text]
    (or (second (re-find #" (\d+) file" input-text)) "0"))

(defn parse-insertions
    [input-text]
    (or (second (re-find #" (\d+) insertions" input-text)) "0"))

(defn parse-deletions
    [input-text]
    (or (second (re-find #" (\d+) deletions" input-text)) "0"))

(defn parse-commit-stat
    [stat]
    {:files-changed (Integer. (parse-files-changed stat))
     :insertions    (Integer. (parse-insertions    stat))
     :deletions     (Integer. (parse-deletions     stat))})

(defn parse-commits-stat
    []
    (let [input (clojure.string/split-lines (slurp "stats"))
          shas (take-nth 2 input)           ; all odd lines
          stats (take-nth 2 (rest input))   ; all even lines
          ]
          (zipmap
              (for [sha shas]
                   (parse-sha sha))
              (for [stat stats]
                   (parse-commit-stat stat)))))

(defn get-commits-stat-for-branch
    [directory-name branch]
    (exec/exec "./scripts/get-commits-stat.sh" directory-name branch)
    (parse-commits-stat))

(defn update-branch-name
    [branch-name]
    (subs branch-name (inc (.lastIndexOf branch-name "/"))))

(defn branch->name
    [branch]
    (update-branch-name (.getName branch)))

(defn get-local-branches
    [repo]
    (for [branch (jgit/git-branch-list repo)]
        (branch->name branch)))

(defn get-commits-stat-for-all-branches
    [repo directory-name]
    (let [branches (get-local-branches repo)]
        (reduce conj
            (for [branch branches]
                 (get-commits-stat-for-branch directory-name branch)))))

