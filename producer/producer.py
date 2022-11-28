import json
import socket
import threading
import requests
import sseclient
import time

# Producer is a wrapper for the docker container producer.
# It opens the two sse streams and sends the data to the message broker.
# The sse streams are located at http://localhost:4000/tweets/1 and http://localhost:4000/tweets/2

first_stream_url = 'http://localhost:4000/tweets/1'
second_stream_url = 'http://localhost:4000/tweets/2'

producer_host = 'localhost'
producer_port = 9000


class Producer:
    def __init__(self, sse_url, broker_host, broker_port):
        self.sse_url = sse_url
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.messages_sent = 0
        self.messages_received = 0
        self.work = True
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def forward(self):

        # Forward all the messages to the message broker
        # Open the sse streams
        messages = sseclient.SSEClient(self.sse_url)

        self.socket.connect((self.broker_host, self.broker_port))

        while self.work:
            time.sleep(1)
            try:
                msg = next(messages)  # Exception when stream sends 0 bytes of data
                json.loads(msg.data)  # Exception when msg.data is not a json
            except:
                continue

            try:
                self.socket.send(msg.data.encode('utf-8'))
                print("Sent: " + str(msg.data))
                response = self.socket.recv(1024)
            except OSError:
                return

            self.messages_sent += 1

            if response == b'OK':
                self.messages_received += 1

        print("Connection closed")
        print("Sent " + str(self.messages_sent) + " messages to the message broker")
        print("Message Broker received: " + str(self.messages_received) + " messages")

    def close(self):
        self.work = False
        self.socket.close()


if __name__ == '__main__':
    # Read host and port from the arguments

    producer1 = Producer(first_stream_url, producer_host, producer_port)

    producer_port += 1
    producer2 = Producer(second_stream_url, producer_host, producer_port)

    t1 = threading.Thread(target=producer1.forward)
    t2 = threading.Thread(target=producer2.forward)
    t1.start()
    t2.start()

    try:
        input()

    except KeyboardInterrupt:
        producer1.close()
        producer2.close()
        exit()

    producer1.close()
    producer2.close()
    t1.join()
    t2.join()
    print("Done")
