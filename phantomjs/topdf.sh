#!/bin/bash
../bin/phantomjs --disk-cache=true   --debug=true   ../examples/rasterize.coffee 'http://www.diandidian.com/plan/?statusName=%E8%AE%A1%E5%88%92%E4%B8%AD&planName=%E8%83%8C%E5%8C%85&forpdf=true&userId=51ee3492e4b01d25c924e16b'   j960-x.pdf   A4
