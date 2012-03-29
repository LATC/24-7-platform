#!/usr/bin/python2
'''
Created on Apr 7, 2011

@author: cgueret
'''

import os
import pycurl
import cStringIO
import json
import urllib
import urllib2
import time
import re
import md5
from datetime import datetime
from xml.dom.minidom import parse, parseString

TEST = "http://127.0.0.1:8080/console/api"
PRODUCTION = "http://latc-console.few.vu.nl/api"
SERVER = PRODUCTION

UP_TO_DATE, UPLOADED, UPDATED, DOWNLOADED, ERROR = range(5)
messages = {
    UP_TO_DATE : '[==] %s',
    UPLOADED   : '[++] %s',
    UPDATED    : '[->] %s',
    DOWNLOADED : '[<-] %s',
    ERROR      : '[!!] %s',
}


def push_new_spec(dir, task, content):
	'''
	Push a new file to the server
	'''
	# Call parameters
	data = [
		('api_key', 'aa4967eb8b7a5ccab7dbb57aa2368c7f'),
		('specification', content)
	]
	
	# Read meta information if they are provided
	meta = {}
	meta_file = '%s/%s/README.txt' % (dir, task)
	if os.path.exists(meta_file):
		tmp = map(lambda x:re.sub(r'[\r\n]+', '', x), open(meta_file, 'r').readlines())
		meta = dict((tmp[i * 2], tmp[i * 2 + 1]) for i in range(len(tmp) / 2))
		tmp = task.split('-')
		meta['Title:'] = "%s -> %s (%s)" % (tmp[0], tmp[1], "".join(tmp[2:]))
		if 'Title:' in meta.keys():
			data.append(('title', meta['Title:']))
		if 'Description:' in meta.keys():
			data.append(('description', meta['Description:']))
		if 'Creator:' in meta.keys():
			data.append(('author', meta['Creator:']))

	# Upload the file
	curl = pycurl.Curl()
	response = cStringIO.StringIO()
	curl.setopt(curl.URL, SERVER + "/tasks")
	curl.setopt(curl.POSTFIELDS, urllib.urlencode(data))
	curl.setopt(curl.WRITEFUNCTION, response.write)
	curl.perform()
	if curl.getinfo(pycurl.HTTP_CODE) != 201:
		return None
	
	# Get the ID
	res = json.loads(response.getvalue())
	curl.close()
	return res['id']

def push_update(id, target, content):
	'''
	Push an updated file to the server
	'''
	data = [
	     ('api_key', 'aa4967eb8b7a5ccab7dbb57aa2368c7f'),
	]
	response = cStringIO.StringIO()
	
	curl = pycurl.Curl()
	curl.setopt(curl.WRITEFUNCTION, response.write)
	curl.setopt(curl.URL, SERVER + '/task/' + id + target)
	
	if target == '/configuration':
		# Send the content of configuration for tasks with a PUT
		data.append(('configuration', content))
		body = urllib.urlencode(data)
		request_buffer = cStringIO.StringIO(body)
		curl.setopt(pycurl.READFUNCTION, request_buffer.read)
		curl.setopt(curl.HTTPHEADER, [ 'Content-Type:application/x-www-form-urlencoded; charset=utf-8', 'Expect: ']);
		curl.setopt(curl.UPLOAD, 1)
		curl.setopt(curl.INFILESIZE, len(body))
	else:
		# Send the content of triple sets with a POST
		data.append(('triples', content))
		response = cStringIO.StringIO()
		curl.setopt(curl.POSTFIELDS, urllib.urlencode(data))
		
	curl.perform()
	curl.close()
 
def synchronize_task(dir, task):
	'''
	Push a specific task and return a status code
	'''
	
	# We need to have all the content files to be able to push the spec
	ok = True
	for f in ['spec.xml', 'positive.nt', 'negative.nt']:
		file = '%s/%s/%s' % (dir, task, f)
		ok = ok and os.path.exists(file)
	if not ok:
		return ERROR

	# default status
	status = UP_TO_DATE
	
	#	
	# Push the configuration file of the task
	#
	spec_file = '%s/%s/spec.xml' % (dir, task)
	spec_local = "".join(open(spec_file).readlines())
    	
	# Retrieve the ID from a previous upload
	id = None
	id_file = '%s/%s/id.txt' % (dir, task)
	if os.path.isfile(id_file):
		id = open(id_file).readlines()[0]
	
	if id == None:
		# If there is no ID, upload the file
		id = push_new_spec(dir, task, spec_local)
		if id != None:
			open(id_file, 'w').write(id)
	else:
		# Download the current specification from the server
		spec_server = urllib2.urlopen(SERVER + '/task/' + id + '/configuration').read()
	
		# Compare the two versions
		disk = md5.new(parseString(spec_local).toxml()).hexdigest()
		server = md5.new(parseString(spec_server).toxml()).hexdigest()
		if disk != server:
			push_update(id, '/configuration', spec_local)
			status = UPDATED

	#	
	# If the task is on the server try to push its positive/negative triples
	#
	if id != None:
		for f in  ['positive.nt', 'negative.nt']:
			target = "/tripleset/%s" % f.split('.')[0]
			content_local = "".join(open('%s/%s/%s' % (dir, task, f)).readlines())
			try:
				content_server = urllib2.urlopen(SERVER + '/task/' + id + target).read()
			except:
				content_server = ''
			disk = md5.new(content_local.strip()).hexdigest()
			server = md5.new(content_server.strip()).hexdigest()
			if disk != server:
				push_update(id, target, content_local)
				status = UPDATED

	return status

if __name__ == '__main__':
	'''
	Push the content of a directory to the LATC console
	'''
	dir = '.'
	tasks = [f for f in os.listdir(dir) if os.path.isdir(f)]
	for task in sorted(tasks):
		res = synchronize_task(dir, task)
		print messages.get(res) % task
	
