#!/usr/bin/env python

import sys
import connection
import time
import threading
import random



class SendMessageThread(threading.Thread):

	def __init__(self, conn):
		threading.Thread.__init__(self)
		self.conn = conn


	def run(self):

		while(True):
			data = "python test {0}".format(random.randint(1, 100))
			self.conn.send_data(data)
			time.sleep(0.1)
		


if __name__ == "__main__" :
	conns = []
	clients = []
	for i in range(int(sys.argv[3])):
		con = connection.TelnetConnection(sys.argv[1], int(sys.argv[2]))
		conns.append(con)
		con.connect()
		con.get_server_output()
      		con.send_data(" test user-{0}".format(i))
		con.get_server_output()
		con.send_data("/join a")
		con.get_server_output()
		time.sleep(0.1)

		c = SendMessageThread(con)		
		clients.append(c)
		c.start()
	time.sleep(100)

	for c in range(conns):
		c.close()
