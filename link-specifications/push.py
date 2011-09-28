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
from datetime import datetime
import time
import re
import md5

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

def synchronize_directory(dir):
    '''
    Push the content of a directory to the LATC console
    '''
    tasks = [f for f in os.listdir(dir) if os.path.isdir(f)]
    for task in sorted(tasks):
        res = synchronize_task(dir, task)
        print messages.get(res) % task

def push_new(dir, task, spec):
    '''
    Push a new file to the server
    '''
    meta_file = '%s/%s/README.txt' % (dir, task)
    spec_file = '%s/%s/spec.xml' % (dir, task)
    
    # Read meta information and generate title
    meta = {}
    if os.path.exists(meta_file):
        tmp = map(lambda x:re.sub(r'[\r\n]+', '', x), open(meta_file, 'r').readlines())
        meta = dict((tmp[i * 2], tmp[i * 2 + 1]) for i in range(len(tmp) / 2))
    tmp = task.split('-')
    meta['Title:'] = "%s -> %s (%s)" % (tmp[0], tmp[1], "".join(tmp[2:]))

    # Upload the file
    curl = pycurl.Curl()
    response = cStringIO.StringIO()
    values = [
              ('api_key', 'aa4967eb8b7a5ccab7dbb57aa2368c7f'),
              ('specification', spec),
              ('title', meta['Title:'])
    ]
    if 'Description:' in meta.keys():
        values.append(('description', meta['Description:']))
    if 'Creator:' in meta.keys():
        values.append(('author', meta['Creator:']))
    curl.setopt(curl.URL, SERVER + "/tasks")
    curl.setopt(curl.POSTFIELDS, urllib.urlencode(values))
    curl.setopt(curl.WRITEFUNCTION, response.write)
    curl.perform()

    # Get the ID
    if curl.getinfo(pycurl.HTTP_CODE) != 201:
        print SERVER + "/tasks"
        print curl.getinfo(pycurl.HTTP_CODE)
        print values
        print response.getvalue()
    res = json.loads(response.getvalue())
    curl.close()
    
    return res['id']

def push_update(id, spec):
    '''
    Push an updated file to the server
    '''
    curl = pycurl.Curl()
    data = [
         ('api_key', 'aa4967eb8b7a5ccab7dbb57aa2368c7f'),
         ('configuration', spec)
    ]
    body = urllib.urlencode(data)
    curl.setopt(curl.URL, SERVER + '/task/' + id + '/configuration')
    curl.setopt(curl.HTTPHEADER, [ 'Content-Type:application/x-www-form-urlencoded; charset=utf-8', 'Expect: ']);
    curl.setopt(curl.UPLOAD, 1)
    request_buffer = cStringIO.StringIO(body)
    curl.setopt(pycurl.READFUNCTION, request_buffer.read)
    curl.setopt(curl.INFILESIZE, len(body))
    response = cStringIO.StringIO()
    curl.setopt(curl.WRITEFUNCTION, response.write)
    curl.perform()
    curl.close()
    
def synchronize_task(dir, task):
    '''
    Push a specific task and return a status code
    '''
    spec_file = '%s/%s/spec.xml' % (dir, task)
    spec_local = "".join(open(spec_file).readlines())
    id_file = '%s/%s/id.txt' % (dir, task)
    
    # Retrieve the ID from a previous upload
    id = None
    if os.path.isfile(id_file):
        id = open(id_file).readlines()[0]
        
    # If there is no ID, upload the file
    if id == None:
        id = push_new(dir, task, spec_local)
        open(id_file, 'w').write(id)
        return UPLOADED
    else:
        # Dowload the current specification from the server
        spec_server = urllib2.urlopen(SERVER + '/task/' + id + '/configuration').read()
        
        # Compare the two versions
        disk = md5.new(spec_local).hexdigest()
        server = md5.new(spec_server).hexdigest()
        
        # Push an update if needed
        if disk == server:
            return UP_TO_DATE            
        else:
            #push_update(id, spec_local)
            return UPDATED
        
    # We should not reach that line
    return ERROR


if __name__ == '__main__':
    synchronize_directory('.')


