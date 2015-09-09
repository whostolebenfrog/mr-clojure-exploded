(defproject exploded "1.0.0-SNAPSHOT"
  :description "Exploded service"

  :dependencies [[ch.qos.logback/logback-classic "1.1.3"]
                 [cheshire "5.4.0"]
                 [clj-http "1.1.2"]
                 [clj-time "0.9.0"]
                 [compojure "1.3.4"]
                 [environ "1.0.0"]
                 [com.codahale.metrics/metrics-logback "3.0.2"]
                 [mixradio/graphite-filter "1.0.0"]
                 [mixradio/instrumented-ring-jetty-adapter "1.0.4"]
                 [mixradio/radix "1.0.10"]
                 [net.logstash.logback/logstash-logback-encoder "4.3"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-json "0.3.1"]
                 [ring-middleware-format "0.5.0"]
                 
                 [cljsjs/react "0.13.3-1"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojurescript "0.0-3308" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prone "0.8.2"]
                 [reagent "0.5.0"]
                 [reagent-utils "0.1.5"]
                 [secretary "1.2.3"]]

  :exclusions [commons-logging
               log4j
               org.clojure/clojure
               cljsjs/react-with-addons]

  :plugins [[lein-environ "1.0.0"]
            [lein-release "1.0.5"]
            [lein-ring "0.8.12"]
            [lein-asset-minifier "0.2.2"]]
  
  :source-paths ["src/clj" "src/cljs"]
  
  :env {:auto-reload "true"
        :environment-name "poke"
        :graphite-enabled "false"
        :graphite-host ""
        :graphite-port "2003"
        :graphite-post-interval-seconds "60"
        :logging-consolethreshold "info"
        :logging-filethreshold "info"
        :logging-level "info"
        :logging-path "/tmp"
        :logging-stashthreshold "off"
        :production "false"
        :requestlog-enabled "false"
        :requestlog-retainhours "24"
        :restdriver-port "8081"
        :service-name "exploded"
        :service-port "8080"
        :service-url "http://localhost:%s"
        :shutdown-timeout-millis "5000"
        :start-timeout-seconds "120"
        :threads "254"}
  
  :lein-release {:deploy-via :shell
                 :shell ["lein" "do" "clean," "uberjar," "pom," "rpm"]}

  :ring {:handler exploded.web/app
         :main exploded.setup
         :port ~(Integer/valueOf (get (System/getenv) "SERVICE_PORT" "8080"))
         :init exploded.setup/setup
         :browser-uri "/healthcheck"
         :nrepl {:start? true}}

  :uberjar-name "exploded.jar"
  
  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :asset-path "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}
  
  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]
  
  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}
  
  :profiles {:dev {:dependencies [[com.github.rest-driver/rest-client-driver "1.1.42"
                                   :exclusions [org.slf4j/slf4j-nop
                                                javax.servlet/servlet-api
                                                org.eclipse.jetty.orbit/javax.servlet]]
                                  [junit "4.12"]
                                  [midje "1.6.3"]
                                  [rest-cljer "0.1.20"]
                                  [lein-figwheel "0.3.7"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   
                   :plugins [[lein-kibit "0.0.8"]
                             [lein-midje "3.1.3"]
                             [lein-rpm "0.0.5"]
                             [lein-figwheel "0.3.7"]
                             [lein-cljsbuild "1.0.6"]]
                   
                   :env {:dev-mode true}
                   
                   :repl-options {:init-ns exploded.repl}
                   
                   :source-paths ["env/dev/clj"]

                   :figwheel {:http-server-root "public"
                              :server-port 8080
                              :nrepl-port 7002
                              :css-dirs ["resources/public/css"]
                              :ring-handler exploded.web/app}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "exploded.dev"
                                                         :source-map true}}}}}
             
             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}}
  
  :rpm {:name "exploded"
        :summary "RPM for Exploded service"
        :copyright "MixRadio 2015"
        :preinstall {:scriptFile "scripts/rpm/preinstall.sh"}
        :postinstall {:scriptFile "scripts/rpm/postinstall.sh"}
        :preremove {:scriptFile "scripts/rpm/preremove.sh"}
        :postremove {:scriptFile "scripts/rpm/postremove.sh"}
        :requires ["jdk >= 2000:1.7.0_55-fcs"]
        :mappings [{:directory "/usr/local/exploded"
                    :filemode "444"
                    :username "exploded"
                    :groupname "exploded"
                    :sources {:source [{:location "target/exploded.jar"}]}}
                   {:directory "/usr/local/exploded/bin"
                    :filemode "744"
                    :username "exploded"
                    :groupname "exploded"
                    :sources {:source [{:location "scripts/bin"}]}}
                   {:directory "/etc/rc.d/init.d"
                    :filemode "755"
                    :sources {:source [{:location "scripts/service/exploded"
                                        :destination "exploded"}]}}]}


  :aot [exploded.setup]

  :main exploded.setup)
