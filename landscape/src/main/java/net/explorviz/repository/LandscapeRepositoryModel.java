package net.explorviz.repository;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;
import org.nustaq.serialization.FSTConfiguration;

import explorviz.live_trace_processing.reader.IPeriodicTimeSignalReceiver;
import explorviz.live_trace_processing.reader.TimeSignalReader;
import explorviz.live_trace_processing.record.IRecord;
import net.explorviz.model.landscape.Landscape;
import net.explorviz.server.helper.BroadcastService;
import net.explorviz.server.helper.PropertyService;
import net.explorviz.server.main.Configuration;

@Service
@Singleton
public final class LandscapeRepositoryModel implements IPeriodicTimeSignalReceiver {

	private static final boolean LOAD_LAST_LANDSCAPE_ON_LOAD = false;
	private volatile Landscape lastPeriodLandscape;
	private final Landscape internalLandscape;
	private final FSTConfiguration fstConf;
	private final InsertionRepositoryPart insertionRepositoryPart;
	private final RemoteCallRepositoryPart remoteCallRepositoryPart;

	private final BroadcastService broadcastService;

	@Inject
	public LandscapeRepositoryModel(final BroadcastService broadcastService) {

		this.broadcastService = broadcastService;

		fstConf = initFSTConf();

		if (LOAD_LAST_LANDSCAPE_ON_LOAD) {

			final Landscape readLandscape = RepositoryStorage.readFromFile(java.lang.System.currentTimeMillis(),
					Configuration.LANDSCAPE_REPOSITORY);

			internalLandscape = readLandscape;
		} else {
			internalLandscape = new Landscape();
			internalLandscape.initializeID();
		}

		insertionRepositoryPart = new InsertionRepositoryPart();
		remoteCallRepositoryPart = new RemoteCallRepositoryPart();

		final Landscape l = fstConf.deepCopy(internalLandscape);

		lastPeriodLandscape = LandscapePreparer.prepareLandscape(l);

		new TimeSignalReader(TimeUnit.SECONDS.toMillis(Configuration.outputIntervalSeconds), this).start();
	}

	public Landscape getLastPeriodLandscape() {
		synchronized (lastPeriodLandscape) {
			return lastPeriodLandscape;
		}
	}

	public Landscape getLandscape(final long timestamp, final String folderName) throws FileNotFoundException {
		return LandscapePreparer.prepareLandscape(RepositoryStorage.readFromFile(timestamp, folderName));
	}

	public Map<Long, Long> getAvailableLandscapes(final String folderName) {
		return RepositoryStorage.getAvailableModelsForTimeshift(folderName);
	}

	static {
		Configuration.DATABASE_NAMES.add("hsqldb");
		Configuration.DATABASE_NAMES.add("postgres");
		Configuration.DATABASE_NAMES.add("db2");
		Configuration.DATABASE_NAMES.add("mysql");
		Configuration.DATABASE_NAMES.add("neo4j");
		Configuration.DATABASE_NAMES.add("database");
		Configuration.DATABASE_NAMES.add("hypersql");
	}

	public FSTConfiguration initFSTConf() {
		return RepositoryStorage.createFSTConfiguration();
	}

	public void reset() {
		synchronized (internalLandscape) {
			internalLandscape.reset();
		}
	}

	@Override
	public void periodicTimeSignal(final long timestamp) {
		// called every tenth second
		// passed timestamp is nanosecond
		synchronized (internalLandscape) {
			synchronized (lastPeriodLandscape) {

				final long milliseconds = java.lang.System.currentTimeMillis();

				if (PropertyService.getBooleanProperty("useDummyMode")) {
					final Landscape dummyLandscape = LandscapeDummyCreator.createDummyLandscape();
					dummyLandscape.setTimestamp(milliseconds);
					RepositoryStorage.writeToFile(dummyLandscape, milliseconds, Configuration.LANDSCAPE_REPOSITORY);
					lastPeriodLandscape = dummyLandscape;
				} else {
					internalLandscape.updateTimestamp(milliseconds);
					RepositoryStorage.writeToFile(internalLandscape, milliseconds, Configuration.LANDSCAPE_REPOSITORY);
					final Landscape l = fstConf.deepCopy(internalLandscape);
					lastPeriodLandscape = LandscapePreparer.prepareLandscape(l);
				}
				// TODO broadcast here
				broadcastService.broadcastMessage(lastPeriodLandscape);
				remoteCallRepositoryPart.checkForTimedoutRemoteCalls();
				resetCommunication();
			}
		}

		RepositoryStorage.cleanUpTooOldFiles(java.lang.System.currentTimeMillis(), Configuration.LANDSCAPE_REPOSITORY);
	}

	private void resetCommunication() {
		internalLandscape.reset();
	}

	public void insertIntoModel(final IRecord inputIRecord) {
		// called every second
		insertionRepositoryPart.insertIntoModel(inputIRecord, internalLandscape, remoteCallRepositoryPart);
	}
}
