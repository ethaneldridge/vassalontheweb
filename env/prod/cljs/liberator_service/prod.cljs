(ns liberator-service.prod
  (:require [liberator-service.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
