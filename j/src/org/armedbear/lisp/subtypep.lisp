;;; subtypep.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: subtypep.lisp,v 1.1 2003-09-13 17:17:42 piso Exp $
;;;
;;; This program is free software; you can redistribute it and/or
;;; modify it under the terms of the GNU General Public License
;;; as published by the Free Software Foundation; either version 2
;;; of the License, or (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with this program; if not, write to the Free Software
;;; Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

(in-package "SYSTEM")

(defparameter *known-types* (make-hash-table))

(dolist (i '((BASE-STRING SIMPLE-STRING)
             (BIGNUM INTEGER)
             (BIT INTEGER)
             (BIT-VECTOR VECTOR)
             (BOOLEAN SYMBOL)
             (CHARACTER ATOM)
             (COMPLEX NUMBER)
             (CONS LIST)
             (EXTENDED-CHAR CHARACTER NIL)
             (FIXNUM INTEGER)
             (FLOAT REAL)
             (INTEGER RATIONAL)
             (KEYWORD SYMBOL)
             (LIST SEQUENCE)
             (NULL SYMBOL LIST)
             (NUMBER ATOM)
             (PACKAGE-ERROR ERROR)
             (PROGRAM-ERROR ERROR)
             (RATIO RATIONAL)
             (RATIONAL REAL)
             (REAL NUMBER)
             (SERIOUS-CONDITION CONDITION)
             (SIMPLE-ARRAY ARRAY)
             (SIMPLE-BASE-STRING SIMPLE-STRING BASE-STRING)
             (SIMPLE-BIT-VECTOR BIT-VECTOR SIMPLE-ARRAY)
             (SIMPLE-STRING STRING SIMPLE-ARRAY)
             (SIMPLE-VECTOR VECTOR SIMPLE-ARRAY)
             (STANDARD-CHAR CHARACTER)
             (STRING VECTOR)
             (SYMBOL ATOM)
             (TWO-WAY-STREAM STREAM)
             (TYPE-ERROR ERROR)
             (VECTOR ARRAY SEQUENCE)
             ))
  (setf (gethash (car i) *known-types*) (cdr i)))

(defun supertypes (type)
  (values (gethash type *known-types*)))

(defun known-type-p (type)
  (if (values (gethash type *known-types*)) t nil))

(defun normalize-type (type)
  (let (tp i)
    (if (consp type)
        (setq tp (car type) i (cdr type))
        (setq tp type i nil))
    (case tp
      ((ARRAY SIMPLE-ARRAY)
       (when (and i (eq (car i) nil))
         (if (eq tp 'simple-array)
             (setq tp 'simple-string)
             (setq tp 'string))
         (when (cadr i)
           (if (consp (cadr i))
               (setq i (cadr i))
               (setq i (list (cadr i)))))))
      (BASE-CHAR
       (setq tp 'character))
      ((SHORT-FLOAT SINGLE-FLOAT DOUBLE-FLOAT LONG-FLOAT)
       (setq tp 'float)))
    (cons tp i)))

(defun simple-subtypep (type1 type2)
  (assert (symbolp type1))
  (assert (symbolp type2))
  (if (memq type2 (supertypes type1))
      t
      (dolist (supertype (supertypes type1))
        (when (simple-subtypep supertype type2)
          (return t)))))

(defun sub-interval-p (i1 i2)
  (let (low1 high1 low2 high2)
    (if (null i1)
        (setq low1 '* high1 '*)
        (if (null (cdr i1))
            (setq low1 (car i1) high1 '*)
            (setq low1 (car i1) high1 (cadr i1))))
    (if (null i2)
        (setq low2 '* high2 '*)
        (if (null (cdr i2))
            (setq low2 (car i2) high2 '*)
            (setq low2 (car i2) high2 (cadr i2))))
    (when (and (consp low1) (integerp (car low1)))
      (setq low1 (1+ (car low1))))
    (when (and (consp low2) (integerp (car low2)))
      (setq low2 (1+ (car low2))))
    (when (and (consp high1) (integerp (car high1)))
      (setq high1 (1- (car high1))))
    (when (and (consp high2) (integerp (car high2)))
      (setq high2 (1- (car high2))))
    (cond ((eq low1 '*)
	   (unless (eq low2 '*)
	           (return-from sub-interval-p nil)))
          ((eq low2 '*))
	  ((consp low1)
	   (if (consp low2)
	       (when (< (car low1) (car low2))
		     (return-from sub-interval-p nil))
	       (when (< (car low1) low2)
		     (return-from sub-interval-p nil))))
	  ((if (consp low2)
	       (when (<= low1 (car low2))
		     (return-from sub-interval-p nil))
	       (when (< low1 low2)
		     (return-from sub-interval-p nil)))))
    (cond ((eq high1 '*)
	   (unless (eq high2 '*)
	           (return-from sub-interval-p nil)))
          ((eq high2 '*))
	  ((consp high1)
	   (if (consp high2)
	       (when (> (car high1) (car high2))
		     (return-from sub-interval-p nil))
	       (when (> (car high1) high2)
		     (return-from sub-interval-p nil))))
	  ((if (consp high2)
	       (when (>= high1 (car high2))
		     (return-from sub-interval-p nil))
	       (when (> high1 high2)
		     (return-from sub-interval-p nil)))))
    (return-from sub-interval-p t)))

(defun subtypep (type1 type2)
  (setq type1 (normalize-type type1)
        type2 (normalize-type type2))
  (when (equal type1 type2)
    (return-from subtypep (values t t)))
  (let ((t1 (car type1))
        (t2 (car type2))
        (i1 (cdr type1))
        (i2 (cdr type2)))
    (unless (or i1 i2)
      (cond ((or (eq t1 nil) (eq t2 t))
             (return-from subtypep (values t t)))
            ((eq t2 nil)
             (return-from subtypep (values nil t)))
            (t
             (return-from subtypep (values (simple-subtypep t1 t2) t)))))
    (cond ((eq t2 'sequence)
           (values (simple-subtypep t2 t2) t))
          ((eq t2 'simple-string)
           (if (memq t1 '(simple-string simple-base-string))
               (if (or (null i2) (eq (car i2) '*))
                   (values t t)
                   (values nil t))
               (values nil (known-type-p t2))))
          (t
           (cond ((eq t1 'float)
           (if (eq t2 'float)
               (values (sub-interval-p i1 i2) t)
               (values nil (known-type-p t2))))
          ((eq t1 'integer)
           (if (member t2 '(integer rational))
               (values (sub-interval-p i1 i2) t)
               (values nil (known-type-p t2))))
          ((eq t1 'rational)
           (if (member t2 '(rational real))
               (values (sub-interval-p i1 i2) t)
               (values nil (known-type-p t2))))
          ((memq t1 '(string simple-string base-string simple-base-string))
           (cond ((eq t2 'string)
                  (if (or (null i2) (eq (car i2) '*))
                      (values t t)
                      (values nil t)))
                 (t
                  (values nil (known-type-p t2)))))
          (t
           (values nil nil)))))))
