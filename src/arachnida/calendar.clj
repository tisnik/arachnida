(ns arachnida.calendar)

(defn get-calendar
    "Gets a calendar using the default time zone and locale."
    []
    (java.util.Calendar/getInstance))

(defn get-calendar-for-week
    "Gets a calendar for given year."
    [year week]
    (let [cal (get-calendar)]
        (.clear cal)
        (.set cal java.util.Calendar/WEEK_OF_YEAR week)
        (.set cal java.util.Calendar/YEAR year)
        cal))

(defn get-year
    "Returns the year for the current date."
    [calendar]
    (.get calendar java.util.Calendar/YEAR))

(defn get-week
    "Returns week number within the current year."
    [calendar]
    (.get calendar java.util.Calendar/WEEK_OF_YEAR))

(defn get-day-of-week
    "Returns day of week for given date."
    [calendar]
    (.get calendar java.util.Calendar/DAY_OF_WEEK))

(defn calculate-first-day-of-week
    "Calculate first day of week for given date."
    [calendar]
    (- (.getFirstDayOfWeek calendar) (get-day-of-week calendar)))

(defn get-first-day-of-week
    "Returns first day of given week."
    [calendar]
    (let [first-day-of-week (.clone calendar)]
        (.add first-day-of-week java.util.Calendar/DAY_OF_WEEK (calculate-first-day-of-week first-day-of-week ))
        first-day-of-week))

(defn get-last-day-of-week
    "Return last day of given week."
    [calendar]
    (let [first-day-of-week (get-first-day-of-week calendar)
          last-day-of-week  (.clone first-day-of-week)]
        (.add last-day-of-week java.util.Calendar/DAY_OF_YEAR, 6)
        last-day-of-week))

(defn format-date-using-desired-format
    "Format given date using desired format, for example 'yyyy-MM-dd' etc."
    [calendar desired-format]
    (let [date-format (new java.text.SimpleDateFormat desired-format)]
        (.format date-format (.getTime calendar))))

(defn format-date
    "Format given date using the following format: 'yyyy-MM-dd'"
    [calendar]
    (format-date-using-desired-format calendar "yyyy-MM-dd"))

(defn get-first-day-of-week-formatted
    "Get date of first day of given week formatted as yyyy-mm-dd."
    [calendar]
    (format-date (get-first-day-of-week calendar)))

(defn get-last-day-of-week-formatted
    "Get date of last day of given week formatted as yyyy-mm-dd."
    [calendar]
    (format-date (get-last-day-of-week calendar)))

(defn format-from-to-date
    [week]
    (let [cal (get-calendar-for-week 2015 week)]
        (str (get-first-day-of-week-formatted cal) " - " (get-last-day-of-week-formatted cal))))
