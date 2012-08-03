#!/usr/bin/python2

import os
import re


if __name__ == '__main__':
	'''
	Push the content of a directory to the LATC console
	'''
	print "Name,Format,Author,Author_guessed,Title,Description,Links,Positive,Negative"
	tasks = [f for f in os.listdir('.') if os.path.isdir(f)]
	for task in sorted(tasks):
		t_name = task
		
		t_spec = "None"
		if os.path.isfile("%s/spec.xml" % task):
			t_spec = "SILK"
		elif os.path.isfile("%s/spec.limes.xml" % task):
			t_spec = "LIMES"
			
		t_author = "Unknown"
		t_guessed = "Unknown"
		t_description = "No description"
		t_title = "No title"
		if os.path.exists("%s/README.txt" % task):
			tmp = map(lambda x:re.sub(r'[\r\n]+', '', x), open("%s/README.txt" % task, 'r').readlines())
			meta = dict((tmp[i * 2], tmp[i * 2 + 1]) for i in range(len(tmp) / 2))
			tmp = task.split('-')
			meta['Title:'] = "%s -> %s (%s)" % (tmp[0], tmp[1], "".join(tmp[2:]))
			if 'Title:' in meta.keys():
				t_title = meta['Title:']
			if 'Description:' in meta.keys():
				t_description = meta['Description:']
			if 'Creator:' in meta.keys():
				t_author = meta['Creator:']
		
		# get the author from the log
		if t_author == "Unknown" and t_spec == "SILK":
			os.system("git log '%s/spec.xml' | grep Author | head -n 1 > /tmp/name.txt" % task)
			txt = open("/tmp/name.txt", 'r').readlines()[0]
			t_guessed = re.search(': ([^<]*) <', txt).group(1)
		else:
			t_guessed = t_author
				
		t_links = ""
		t_positive = ""
		t_negative = ""
		if os.path.exists("%s/links.nt" % task):
			t_links = str(len(open("%s/links.nt" % task).readlines()))
		if os.path.exists("%s/positive.nt" % task):
			t_positive = str(len(open("%s/positive.nt" % task).readlines()))
		if os.path.exists("%s/negative.nt" % task):
			t_negative = str(len(open("%s/negative.nt" % task).readlines()))
			
		line = t_name
		line += "," + t_spec
		line += "," + t_author.replace(',',' and ')
		line += "," + t_guessed.replace(',',' and ')
		line += "," + t_title
		line += "," + t_description
		line += "," + t_links
		line += "," + t_positive
		line += "," + t_negative
		print line
		
		
