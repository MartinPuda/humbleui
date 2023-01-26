(defproject humbleui "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0-alpha1"]
                 [io.github.humbleui/types "0.2.0" :classifier "clojure"]
                 [io.github.humbleui/jwm "0.4.12" :exclusions [io.github.humbleui/types]]
                 [io.github.humbleui/skija-shared "0.109.0" :exclusions [io.github.humbleui/types]]
                 [io.github.humbleui/skija-windows "0.109.0" :exclusions [io.github.humbleui/types]]
                 [io.github.humbleui/skija-linux "0.109.0" :exclusions [io.github.humbleui/types]]
                 [io.github.humbleui/skija-macos-x64 "0.109.0" :exclusions [io.github.humbleui/types]]
                 [io.github.humbleui/skija-macos-arm64 "0.109.0" :exclusions [io.github.humbleui/types]]
                 [humbleui "0e93d9a6172caff3bf58eb00f5fbef4e30a977cb"]]
  :plugins [[reifyhealth/lein-git-down "0.4.1"]]
  :repositories [["public-github" {:url "git://github.com"}]]
  :git-down {humbleui {:coordinates HumbleUI/HumbleUI}}
  :middleware [lein-git-down.plugin/inject-properties]
  :main ^:skip-aot humbleui.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
