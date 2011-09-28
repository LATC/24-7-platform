#!/usr/bin/python2

import os

if __name__ == '__main__':
    total_links = 0
    negative_links = 0
    tasks = [f for f in os.listdir('.') if os.path.isdir(f)]
    for task in sorted(tasks):
        links = "%s/links.nt" % task
        if os.path.isfile(links):
            total_links += len(open(links).readlines())
        negative = "%s/negative.nt" % task
        if os.path.isfile(negative):
            negative_links += len(open(negative).readlines())
    print "%d links" % total_links
    print "%d negative links" % negative_links
    