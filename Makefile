JC=javac

all: Frame.class Node.class Switch.class Main.class CASListenerThread.class CASSwitch.class CCSListenerThread.class CCSSwitch.class MonitorNode.class NodeListener.class RelayNode.class

Frame.class: Frame.java
	$(JC) Frame.java
	
CASListenerThread.class: CASListenerThread.java
	$(JC) CASListenerThread.java
	
CCSListenerThread.class: CCSListenerThread.java
	$(JC) CCSListenerThread.java
	
CASSwitch.class: CASSwitch.java
	$(JC) CASSwitch.java
	
CCSSwitch.class: CCSSwitch.java
	$(JC) CCSSwitch.java
	
Main.class: Main.java
	$(JC) Frame.java
	
MonitorNode.class: MonitorNode.java
	$(JC) MonitorNode.java

Node.class: Node.java
	$(JC) Node.java
	
NodeListener.class: NodeListener.java
	$(JC) NodeListener.java
	
RelayNode.class: RelayNode.java
	$(JC) RelayNode.java

clean:
	rm -f Frame.class Node.class Switch.class Main.class CASListenerThread.class CASSwitch.class CCSListenerThread.class CCSSwitch.class MonitorNode.class NodeListener.class RelayNode.class

