(ns arachnida.db-spec
    "Namespace that contains configuration of all JDBC sources.")

(def data-db
    {:classname   "org.sqlite.JDBC"
     :subprotocol "sqlite"
     :subname     "data.db"
    })

