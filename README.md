# 4390Protocol

# Math

Math is of the form `[0-9]+[\-+*/][0-9]+`. That is, `digits` `symbol` `digits`. It can do addition, subtraction, multiplication and division. It handles division by zero correctly.

# Protocol

The protocol is as follows:

Messages are newline-terminates byte-arrays. 

The first byte is the numeric ID type of the messages (ID 1 = 0x31, NOT 0x01).

The second byte is a numeric value describing the message, used for debugging.

The remainder is a body of text that accompanies the message.

Clients send the message (ID=1,body=username) to join the server, which will ignore their messages otherwise.

Clients wait until they receive that message back as confirmation they are connected before sending more messages. [NOTE: NO THEY DONT!!! BUT THEY SHOULD]

Clients send keepalives (ID=4,body=empty) to keep the TCP connection alive.

Clients send the message (ID=3,body=math) to request the solution to that math problem.

The server replies to that messages with (ID=5,body=math + "=" + solution).

To disconnect, the Client sends (ID=2,body=username). 




# TODO

Server-side logging.

A good read me.

That note above.

Instructions on how to run it I don't understand java.

Less magic numbers. Enum the message ids or something. idk tired.
