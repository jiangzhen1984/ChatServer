#!/usr/bin/env python


import socket

class TelnetConnection:


	def __init__(self, host, port):
		self.host = host
		self.port = port
		self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


	def connect(self):
		try:
			self.socket.connect((self.host, self.port))
		except:
			print "can not connect server", self.host, self.port
			return False;
		return True


	def send_data(self, msg):
		self.socket.send(msg)

	def get_server_output(self):
		return self.socket.recv(1024)


	def close(self):
		self.socket.close()


	
