package org.strangeforest.tcb.dataload

import org.assertj.core.data.*
import org.junit.*

import static org.assertj.core.api.Assertions.*
import static org.strangeforest.tcb.dataload.EloRatings.*

class EloRatingsTest {

	@Test
	void "Start rating is calculated correctly"() {
		assert startRating(  1) == 2368
		assert startRating(  2) == 2288
		assert startRating(  3) == 2234
		assert startRating(  4) == 2191
		assert startRating(  5) == 2157
		assert startRating(  6) == 2132
		assert startRating(  7) == 2108
		assert startRating(  8) == 2092
		assert startRating(  9) == 2077
		assert startRating( 10) == 2062
		assert startRating( 40) == 1879
		assert startRating(125) == 1648
		assert startRating(250) == 1516
		assert startRating(500) == 1500
	}

	static startRating(int rank) {
		new EloRating(100, rank, null).rating
	}

	@Test
	void "kFactor is determined correctly"() {
		def offset = Offset.offset((double)0.000001)
		
		assertThat(kFactor('G', null, 'F', (short)5, null)).isCloseTo((double)100.0, offset)
		assertThat(kFactor('G', null, 'SF', (short)5, null)).isCloseTo((double)95.0, offset)
		assertThat(kFactor('G', null, 'QF', (short)5, null)).isCloseTo((double)90.0, offset)
		assertThat(kFactor('G', null, 'R16', (short)5, null)).isCloseTo((double)85.0, offset)
		assertThat(kFactor('G', null, 'R32', (short)5, null)).isCloseTo((double)80.0, offset)
		assertThat(kFactor('G', null, 'R64', (short)5, null)).isCloseTo((double)75.0, offset)
		assertThat(kFactor('G', null, 'R128', (short)5, null)).isCloseTo((double)70.0, offset)

		assertThat(kFactor('F', null, 'F', (short)3, null)).isCloseTo((double)81.0, offset)
		assertThat(kFactor('F', null, 'SF', (short)3, null)).isCloseTo((double)76.95, offset)
		assertThat(kFactor('F', null, 'QF', (short)3, null)).isCloseTo((double)72.9, offset)
		assertThat(kFactor('F', null, 'RR', (short)3, null)).isCloseTo((double)72.9, offset)

		assertThat(kFactor('M', null, 'F', (short)5, null)).isCloseTo((double)80.0, offset)
		assertThat(kFactor('M', null, 'F', (short)3, null)).isCloseTo((double)72.0, offset)
		assertThat(kFactor('M', null, 'SF', (short)3, null)).isCloseTo((double)68.4, offset)
		assertThat(kFactor('M', null, 'QF', (short)3, null)).isCloseTo((double)64.8, offset)
		assertThat(kFactor('M', null, 'R16', (short)3, null)).isCloseTo((double)61.2, offset)
		assertThat(kFactor('M', null, 'R32', (short)3, null)).isCloseTo((double)57.6, offset)
		assertThat(kFactor('M', null, 'R64', (short)3, null)).isCloseTo((double)54.0, offset)
		assertThat(kFactor('M', null, 'R128', (short)3, null)).isCloseTo((double)50.4, offset)

		assertThat(kFactor('O', null, 'F', (short)3, null)).isCloseTo((double)67.5, offset)
		assertThat(kFactor('O', null, 'BR', (short)3, null)).isCloseTo((double)65.8125, offset)
		assertThat(kFactor('O', null, 'SF', (short)3, null)).isCloseTo((double)64.125, offset)
		assertThat(kFactor('O', null, 'QF', (short)3, null)).isCloseTo((double)60.75, offset)
		assertThat(kFactor('O', null, 'R16', (short)3, null)).isCloseTo((double)57.375, offset)
		assertThat(kFactor('O', null, 'R32', (short)3, null)).isCloseTo((double)54.0, offset)
		assertThat(kFactor('O', null, 'R64', (short)3, null)).isCloseTo((double)50.625, offset)

		assertThat(kFactor('A', null, 'F', (short)3, null)).isCloseTo((double)63.0, offset)
		assertThat(kFactor('A', null, 'SF', (short)3, null)).isCloseTo((double)59.85, offset)
		assertThat(kFactor('A', null, 'QF', (short)3, null)).isCloseTo((double)56.7, offset)
		assertThat(kFactor('A', null, 'R16', (short)3, null)).isCloseTo((double)53.55, offset)
		assertThat(kFactor('A', null, 'R32', (short)3, null)).isCloseTo((double)50.4, offset)
		assertThat(kFactor('A', null, 'R64', (short)3, null)).isCloseTo((double)47.25, offset)

		assertThat(kFactor('B', null, 'F', (short)3, null)).isCloseTo((double)54.0, offset)
		assertThat(kFactor('B', null, 'SF', (short)3, null)).isCloseTo((double)51.3, offset)
		assertThat(kFactor('B', null, 'QF', (short)3, null)).isCloseTo((double)48.6, offset)
		assertThat(kFactor('B', null, 'R16', (short)3, null)).isCloseTo((double)45.9, offset)
		assertThat(kFactor('B', null, 'R32', (short)3, null)).isCloseTo((double)43.2, offset)

		assertThat(kFactor('M', null, 'R32', (short)3, 'W/O')).isCloseTo((double)28.8, offset)
	}

	@Test
	void "kFunction is determined correctly"() {
		def offset = Offset.offset((double)0.000001)

		assertThat(kFunction((double)1500)).isCloseTo((double)1.0, offset)
		assertThat(kFunction((double)1800)).isCloseTo((double)1.0, offset)
		assertThat(kFunction((double)1850)).isCloseTo((double)0.875, offset)
		assertThat(kFunction((double)1900)).isCloseTo((double)0.75, offset)
		assertThat(kFunction((double)1950)).isCloseTo((double)0.625, offset)
		assertThat(kFunction((double)2000)).isCloseTo((double)0.5, offset)
		assertThat(kFunction((double)2300)).isCloseTo((double)0.5, offset)
	}
}