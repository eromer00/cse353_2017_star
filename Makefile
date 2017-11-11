JC=javac

all: Frame.class Node2017.class Switch.class Main.class

Main.class: Main.java
	$(JC) Main.java

Frame.class: Frame.java
	$(JC) Frame.java

Node2017.class: Node2017.java
	$(JC) Node2017.java
	
Switch.class: Switch.java
	$(JC) Switch.java

clean:
	rm -f Frame.class Node2017.class Switch.class Main.class
