;;;; -*- Mode: LISP -*-
(asdf:defsystem :abcl-contrib-version
  :author "Ralph Ritoch"
  :version "${abcl.version}" 
  :description "<> asdf:defsystem <urn:abcl.org/release/${abcl.version}/contrib/version#${abcl.version}>"
  :components ((:module base 
                        :pathname "" :serial t 
                        :components ((:file "version")))))