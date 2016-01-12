;
;  (C) Copyright 2015, 2016  Pavel Tisnovsky
;
;  All rights reserved. This program and the accompanying materials
;  are made available under the terms of the Eclipse Public License v1.0
;  which accompanies this distribution, and is available at
;  http://www.eclipse.org/legal/epl-v10.html
;
;  Contributors:
;      Pavel Tisnovsky
;

(ns arachnida.db-interface)

(require '[clojure.java.jdbc :as jdbc])
(require '[arachnida.db-spec :as db-spec])

(defn read-product-list
    []
    (jdbc/query db-spec/data-db  "select id, name
                                  from   products
                                  order  by id;"))

(defn read-product-names
    []
    (jdbc/query db-spec/data-db  "select name
                                  from   products
                                  order  by name;"))

(defn read-product-id
    [product-name]
    (:id (first (jdbc/query db-spec/data-db ["select id
                                              from products
                                              where name=?" product-name]))))

(defn read-repo-list
    [product-id]
    (jdbc/query db-spec/data-db ["select id, name, url
                                  from   repos
                                  where  product=? order by name;" product-id]))

(defn read-commit-id
    [db product repo sha]
    (:id
    (first
        (jdbc/query db ["select id from commits where product=? and repo=? and sha=?"
                             product repo sha]))))

(defn insert-product
    [db product-name]
    (jdbc/insert! db "products" {:name product-name}))

(defn insert-repository
    [db product-name repository-name repository-url]
    (let [product-id (first (jdbc/query db ["select id from products where name=?;" product-name]))]
        (jdbc/insert! db "repos" {:product (:id product-id) :name repository-name :url repository-url})))

(defn insert-changed-file
    [db commit-id changed-file]
    (jdbc/insert! db "changed_files" {:commit_id commit-id
                                      :file_name (first changed-file)
                                      :operation (second changed-file)}))

(defn insert-branch-for-commit
    [db commit-id branch-name]
    (jdbc/insert! db "branches_for_commit" {:commit_id commit-id
                                           :branch branch-name}))

(defn insert-commit
    "Write informatioun about given commit into database."
    [db product repo sha message author date files-changed insertions deletions]
    (jdbc/insert! db "commits" {:product product :repo repo
                                ;:branch branch ;;; information about branches are stored in the table branches_for_commit
                                :sha     sha     :message message
                                :author author   :date date
                                :files_changed files-changed
                                :insertions insertions
                                :deletions deletions}))

(defn read-author-names
    []
    (jdbc/query db-spec/data-db
                               ["select author
                                 from commits
                                 where date between '2015-01-01' and '2015-12-31'
                                 group by author order by author"]))

(defn read-authors
    []
    (jdbc/query db-spec/data-db
                               ["select author as author,
                                        count(*) as commits_count,
                                        sum(files_changed) as files_changed,
                                        sum(insertions) as insertions,
                                        sum(deletions) as deletions
                                 from commits
                                 where date between '2015-01-01' and '2015-12-31'
                                 group by author order by author"]))

(defn read-statistic-for-author
    [author-name]
    (first
    (jdbc/query db-spec/data-db
                               ["select count(*) as commits_count,
                                        sum(files_changed) as files_changed,
                                        sum(insertions) as insertions,
                                        sum(deletions) as deletions
                                 from commits
                                 where date between '2015-01-01' and '2015-12-31'
                                 and author=?" author-name])))

(defn read-statistic-for-product
    [product-id]
    (first
    (jdbc/query db-spec/data-db
                               ["select count(*) as commits_count,
                                        sum(files_changed) as files_changed,
                                        sum(insertions) as insertions,
                                        sum(deletions) as deletions
                                 from commits
                                 where date between '2015-01-01' and '2015-12-31'
                                 and product=?" product-id])))

(defn read-statistic-for-product-repo
    [product-id]
    (jdbc/query db-spec/data-db
                               ["select count(*) as commits_count,
                                        (select name from repos where repos.id=commits.repo) as reponame,
                                        sum(files_changed) as files_changed,
                                        sum(insertions) as insertions,
                                        sum(deletions) as deletions
                                 from commits
                                 where date between '2015-01-01' and '2015-12-31'
                                 and product=?
                                 group by reponame" product-id]))

(defn read-stat-per-weeks-from-db
    [first-day last-day]
    (jdbc/query db-spec/data-db ["select count(*) as commits_count,
                                         sum(files_changed) as files_changed,
                                         sum(insertions) as insertions,
                                         sum(deletions) as deletions
                                 from commits
                                 where date between ? and ?" first-day last-day]))

(defn read-stat-per-weeks-for-author-from-db
    [first-day last-day author]
    (jdbc/query db-spec/data-db ["select count(*) as commits_count,
                                         sum(files_changed) as files_changed,
                                         sum(insertions) as insertions,
                                         sum(deletions) as deletions
                                 from commits
                                 where date between ? and ? and author=?" first-day last-day author]))

(defn read-statistic-for-week-from-db
    [first-day last-day author]
    (jdbc/query db-spec/data-db ["select repo,
                                         (select name from products where products.id=commits.product) as product,
                                         (select name from repos where repos.id=commits.repo) as reponame,
                                         count(*) as commits,
                                         sum(files_changed) as files,
                                         sum(insertions) as insertions,
                                         sum(deletions) as deletions
                                 from  commits
                                 where date between ? and ?
                                   and author=?
                                 group by repo
                                 order by product, repo" first-day last-day author]))

(defn read-commits-for-week
    [first-day last-day author]
    (jdbc/query db-spec/data-db ["select id,
                                         (select name from products where products.id=commits.product) as product,
                                         (select name from repos where repos.id=commits.repo) as repo,
                                         sha, message, date
                                  from commits
                                  where date between ? and ? and author=?
                                  order by product, repo, date" first-day last-day author]))

(defn read-commit-info
    [commit-id]
    (if commit-id
        (jdbc/query db-spec/data-db ["select * from changed_files where commit_id=?" commit-id])))

(defn read-branches-for-commit
    [commit-id]
    (if commit-id
        (jdbc/query db-spec/data-db ["select * from branches_for_commit where commit_id=?" commit-id])))

