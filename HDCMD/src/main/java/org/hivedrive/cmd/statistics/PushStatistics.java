package org.hivedrive.cmd.statistics;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.time.StopWatch;

public class PushStatistics {
	
	private AtomicLong howManyOriginFilesSent = new AtomicLong();
	private AtomicLong howManyPartsCreated = new AtomicLong();
	private AtomicLong howManyFilesDeleted = new AtomicLong();
	private StopWatch stopwatch = StopWatch.createStarted();
	
	public void fileSent() {
		howManyOriginFilesSent.incrementAndGet();
	}
	
	public void partCreated() {
		howManyPartsCreated.incrementAndGet();
	}
	
	public void partsCreated(int partsNumber) {
		howManyPartsCreated.addAndGet(partsNumber);
	}
	
	public void filesDeleted(int size) {
		howManyFilesDeleted.addAndGet(size);
		
	}
	
	
	public long howManyOriginFilesSent() {
		return howManyOriginFilesSent.longValue();
	}
	public long howManyPartsCreated() {
		return howManyPartsCreated.longValue();
	}
	public long howManyFilesDeleted() {
		return howManyFilesDeleted.longValue();
	}

	public StopWatch getStopwatch() {
		return stopwatch;
	}

	
	
	
	
}
