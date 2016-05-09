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

(ns arachnida.commits-stat-test
  (:require [clojure.test :refer :all]
            [arachnida.commits-stat :refer :all]))

;
; Common functions used by tests.
;

(defn callable?
    "Test if given function-name is bound to the real function."
    [function-name]
    (clojure.test/function? function-name))

;
; Tests for various functions
;

(deftest test-parse-sha-existence
    "Check that the arachnida.commits-stat/parse-sha definition exists."
    (testing "if the arachnida.commits-stat/parse-sha definition exists."
        (is (callable? 'arachnida.commits-stat/parse-sha))))

