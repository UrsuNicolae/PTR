# This is a cli that creates consumers
import json
import os
import random
import socket
import sys
import threading
import time

host = "localhost"
start_port = 10001
subscribe_host = "localhost"
subscribe_port = 9999
from topic_list import get_topic_list

all_topic_list = get_topic_list()


class Consumer:
    def __init__(self, host, port):
        self.host = host
        self.port = port

        num_topics = random.randint(1, 10)
        topic_list = [random.choice(all_topic_list) for _ in range(num_topics)]
        self.topic_set: set[str] = set(topic_list)
        self.received = 0
        self.correct = 0
        self.incorrect = 0
        self.tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.tcp_socket.bind((host, port))
        self.tcp_socket.listen()
        self.close_connection = False

    def consume(self):
        # Listen on address
        print("Listening on " + self.host + ":" + str(self.port))

        try:
            conn, addr = self.tcp_socket.accept()
        except Exception as e:
            self.tcp_socket.close()
            return
        while conn and self.close_connection is False:
            try:
                data = conn.recv(20000)
            except OSError as e:
                return
            data_str = data.decode("utf-8")
            if data_str == "":
                continue
            data_json = json.loads(data_str)
            print("Received: " + str(data_json['message']['tweet']['user']['time_zone']))
            print("Received: " + str(data_json))
            print()
            self.received += 1
            if data_json['message']['tweet']['user']['time_zone'] in self.topic_set:
                self.correct += 1
                #print("Received: " + str(data_json['message']['tweet']['user']['time_zone']))
            else:
                self.incorrect += 1

            if self.received % 100 == 0:
                print("Received: " + str(self.received))

    def subscribe(self):
        subscribe_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        subscribe_socket.connect((subscribe_host, subscribe_port))
        subscribe_socket.send(json.dumps(
            {"operation": "subscribe", "topic": list(self.topic_set),
             "consumer_address": self.host + ':' + str(self.port)}).encode(
            'utf-8'))
        response = subscribe_socket.recv(1024)
        subscribe_socket.close()

    def unsubscribe(self):
        unsubscribe_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        unsubscribe_socket.connect((subscribe_host, subscribe_port))
        unsubscribe_socket.send(json.dumps(
            {"operation": "unsubscribe", "topic": list(self.topic_set),
             "consumer_address": self.host + ':' + str(self.port)}).encode('utf-8'))

        response = unsubscribe_socket.recv(1024)
        unsubscribe_socket.close()

    def close(self):
        self.close_connection = True
        self.tcp_socket.close()

    def print_results(self):
        print("Received: " + str(self.received))
        print("Incorrect: " + str(self.incorrect))


current_num_consumers = 0
consumer_list = []
if __name__ == '__main__':

    print("Type create x to create x consumers with random topics.")

    number = int(input("Input the number of consumers:"))
    for i in range(number):
        port = start_port + current_num_consumers
        current_num_consumers += 1
        consumer_list.append(Consumer(host, port))

    thread_pool = []
    # Create a thread for each consumer and start consume method
    for consumer in consumer_list:
        consumer.subscribe()
        thread = threading.Thread(target=consumer.consume)
        thread_pool.append(thread)
        thread.start()
    try:
        input("Press enter to stop listening:")
    except KeyboardInterrupt:
        print("Interrupting...")
    finally:
        for i in range(len(consumer_list)):
            consumer = consumer_list[i]
            print("Consumer " + str(i) + ":")
            consumer.print_results()

        for consumer in consumer_list:
            consumer.unsubscribe()
            consumer.close()

        for thread in thread_pool:
            thread.join()
