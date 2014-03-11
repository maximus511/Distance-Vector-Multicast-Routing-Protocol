The programming language used in this project is JAVA.

The files involved in this project are-
Host.java
Router.java
Controller.java


Execution related steps-

- All the files need to be in the same location.

- All the files need to be compiled first using 'javac' in console.
  For example- javac Router.java

- For execution of the project, after compilation, the command should be

java Host 0 0 sender 50 20&
java Host 1 1 receiver &
java Router 0 0 1 &
java Router 1 1 2 &
java Router 2 2 3 &
java Router 3 3 0 &
java Controller host 0 1 router 0 1 2 3 lan 0 1 2 3&


The Hout, Rout and lan files will be created in the same folder as the java files.