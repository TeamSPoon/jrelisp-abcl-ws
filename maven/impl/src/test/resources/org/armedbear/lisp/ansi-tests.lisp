;; Just run all tests from the correct path!

(setq *default-pathname-defaults* (truename (merge-pathnames (make-pathname :directory '(:relative :up "ansi-test" "trunk" "ansi-tests")))))

(load "doit.lsp")
