(ns arachnida.git-interface)

(require '[clj-jgit.porcelain :as jgit])
(require '[clj-jgit.querying  :as jgit-query])
(require '[clj-jgit.internal  :as jgit-internal])

(require '[hozumi.rm-rf       :as rm-rf])

(def home-directory
    (System/getProperty "user.home"))

(defn slurp-file-from-homedir-
    [file-name]
    (slurp (str home-directory "/" file-name)))

(defn clone-repository
    "Clone repository into specified directory."
    [url directory]
    (try
        (binding [jgit/*ssh-prvkey* (slurp-file-from-homedir- ".ssh/id_rsa")
                  jgit/*ssh-pubkey* (slurp-file-from-homedir- ".ssh/id_rsa.pub")]
                  (jgit/git-clone url directory))
        (catch Exception e
            (println "*** Exception *** " e)
            nil)))

(defn fetch-all
    [directory-name]
    (try
        (binding [jgit/*ssh-prvkey* (slurp-file-from-homedir- ".ssh/id_rsa")
                  jgit/*ssh-pubkey* (slurp-file-from-homedir- ".ssh/id_rsa.pub")]
                  (jgit/with-repo (new java.io.File directory-name)
                      (jgit/git-fetch-all repo)))
        (catch Exception e
            (println "*** Exception *** " e)
            nil)))

