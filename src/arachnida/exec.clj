;
;  (C) Copyright 2015  Pavel Tisnovsky
;
;  All rights reserved. This program and the accompanying materials
;  are made available under the terms of the Eclipse Public License v1.0
;  which accompanies this distribution, and is available at
;  http://www.eclipse.org/legal/epl-v10.html
;
;  Contributors:
;      Pavel Tisnovsky
;

(ns arachnida.exec)

(defn exec
    "Execute external command and pass given number of parameters to it."
    ; command called without parameters
    ([command]
     (doto (. (Runtime/getRuntime) exec command)
           (.waitFor)
           (.destroy)))
    ; command called with one parameter
    ([command param1]
     (doto (. (Runtime/getRuntime) exec (str command " " param1))
           (.waitFor)
           (.destroy)))
    ; command called with two parameters
    ([command param1 param2]
     (doto (. (Runtime/getRuntime) exec (str command " " param1 " " param2))
           (.waitFor)
           (.destroy)))
    ; command called with three parameters
    ([command param1 param2 param3]
     (doto (. (Runtime/getRuntime) exec (str command " " param1 " " param2 " " param3))
           (.waitFor)
           (.destroy)))
    ; command called with four parameters
    ([command param1 param2 param3 param4]
     (doto (. (Runtime/getRuntime) exec (str command " " param1 " " param2 " " param3 " " param4))
           (.waitFor)
           (.destroy))))

