package org.strangeforest.tcb.dataload

import org.jsoup.*

clearCaches()

static clearCaches() {
	def doc = Jsoup.connect('http://localhost/manage/clearCache').timeout(10 * 1000).get()
	println doc.select('body').text()
}

