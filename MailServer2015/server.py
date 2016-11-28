global dataOn
global allMails
global mail
global name
global mailFrom
global rcptTo
global username
global password
global sock
global connection
def parse(cmd) :#processing the command
	global dataOn
	global allMails
	global mail
	global name
	global mailFrom
	global rcptTo	
	global username
	global password
	global sock
	global connection
	print "cmd = ", cmd
	if dataOn == True :#a new line of a mail
		if cmd == "." :#the end of the mail
			dataOn = False
			print "rcpt = ", rcptTo
			if not allMails.has_key(rcptTo) :
				allMails[rcptTo] = []
			allMails[rcptTo] += [[name, mailFrom, mail]]
		else :
			mail = mail + [cmd]
	elif cmd[0:4].upper() == "HELO" :#HELO name
		print "HELO"
		name = cmd[5:]
	elif cmd[0:9].upper() == "MAIL FROM" :#MAIL FROM: <aaa@bbb.ccc>
		print "MAIL FROM"
		mailFrom = cmd[12:-1]
	elif cmd[0:7].upper() == "RCPT TO" :#RCPT TO: <aaa@bbb.ccc>
		print "RCPT TO"
		rcptTo = cmd[10:-1]
	elif cmd[0:4].upper() == "DATA" :#DATA
		print "DATA"
		dataOn = True
		mail = []
	elif cmd[0:3].upper() == "ALL" :#list all mails on the server. debug command.
		print allMails
	elif cmd[0:4].upper() == "EXIT" :#exit and save the mails to a local file.
		f = open('data.txt', 'w')
		f.write(str(allMails))
		f.close()
		exit()
	elif cmd[0:4].upper() == "USER" :#USER username
		username = cmd[5:]
	elif cmd[0:4].upper() == "PASS" :#PASS password
		password = cmd[5:]
	elif cmd[0:4].upper() == "LIST" :#LIST
		if password == username + "123456" :#identity certification
			if allMails.has_key(username) :
				res = len(allMails[username])
			else :
				res = 0
			connection.send("total " + str(res) + " mails, numbered from 1.\n")
		else :
			connection.send("Mismatch.\n")
	elif cmd[0:4].upper() == "RETR" :#RETR x
		if password == username + "123456" :#identity certification
			if allMails.has_key(username) :
				res = len(allMails[username])
			else :
				res = 0
			if len(cmd) < 6 or cmd[5] < '0' or cmd[5] > '9' :#robust check
				connection.send('Invalid mail index\n.\n')
				return
			x = int(cmd[5:]) - 1
			print res
			if x >= res or x < 0 :#robust check
				connection.send("Invalid mail index\n.\n")
			else :
				curMail = allMails[username][x][2]
				for i in curMail:
					connection.send(i + '\n')
				connection.send("\n.\n")
		else :
			connection.send("Mismatch\n.\n")
	elif cmd[0:4].upper() == "DELE" :#DELE x
		if password == username + "123456" :#identity certification
			if allMails.has_key(username) :
				res = len(allMails[username])
			else :
				res = 0
			if cmd[5] < '0' or cmd[5] > '9' :#robust check
				return
			x = int(cmd[5:]) - 1
			print res
			if x >= res or x < 0 : #robust check
				connection.send("Invalid mail index.\n")
			else :
				allMails[username] = allMails[username][0:x] + allMails[username][x + 1:]
				connection.send("ok total " + str(x) + " mails.\n")
		else :
			connection.send("Mismatch.\n")
global dataOn
global allMails
global name
global mailFrom
global rcptTo
global sock
global username
global password
global connection
username = ""
password = ""
name = ""
mailFrom = ""
rcptTo = ""
allMails = {}
import socket
print "Mail Server Started"
f = open('data.txt', 'r')#read mails from a local file to initiate the server
allMails = eval(f.read())
print allMails
f.close()
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('localhost', 7073))#listening at 127.0.0.1, 7073.
sock.listen(5)
cmd = ""
print "Listening socket 7073"
while True :
	connection, address = sock.accept()#accept a connection
	connection.send('\n')
	print connection
	print address
	dataOn = False
	while True :
		buf = connection.recv(1)#receive one charactor once
		
		print buf, buf == '\n', buf == '\r'
		if buf == '\n' or buf == '\r' :#when receiving a "Enter", process the command
			
			if cmd != "\n" and cmd != "\r" :
				parse(cmd)
				cmd = ""
		else :
			cmd = cmd + buf
			#connection.send(buf)
			#connection.send('-')
