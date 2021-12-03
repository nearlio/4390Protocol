all:
	javac Message.java TCPClient.java TCPServer.java

server:
	java TCPServer

client:
	java TCPClient

clean:
	rm *.class