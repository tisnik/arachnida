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

(ns arachnida.db-spec
    "Namespace that contains configuration of all JDBC sources.")

(def data-db
    {:classname   "org.sqlite.JDBC"
     :subprotocol "sqlite"
     :subname     "data.db"
    })

