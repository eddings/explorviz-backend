package net.explorviz.landscape.repository.persistence;

import java.util.List;
import net.explorviz.shared.landscape.model.landscape.Landscape;

public interface ReplayRepository<T> {


  /**
   * Save a landscape as replay in the repository.
   *
   * @param timestamp the timestamp
   * @param replay the replay landscape
   * @param totalRequests the total amount of requests
   */
  void save(final long timestamp, final Landscape replay, int totalRequests);

  /**
   * Retrieves the total requests of a replay.
   *
   * @param timestamp the timestamp of the replay
   * @return the total requests
   */
  int getTotalRequests(long timestamp);


  /**
   * Retrieves all timestamps currently stored in the db. Each timestamp is a unique identifier of
   * an object.
   *
   * @return list of all timestamps
   */
  List<Long> getAllTimestamps();

  /**
   * Retrieves a replay landscape object with a specific timestamp from the repository.
   *
   * @param timestamp the timestamp of the replay
   *
   * @return the landscape object
   */
  T getByTimestamp(final long timestamp);

  /**
   * Retrieves a replay object with a specific, unique identifier.
   *
   * @param id the id of the landscape object
   *
   * @return the replay object
   */
  T getById(final long id);

  /**
   * Removes all landscapes that have exceeded their lifespan.
   *
   * @param from the reference timestamp to use for lifetime calculation.
   */
  void cleanup(final long from);

  /**
   * Removes all landscapes that have exceeded their life span. Equivalent to
   * {@code cleanup(System.currentTimeMillis())}
   *
   */
  default void cleanup() {
    cleanup(System.currentTimeMillis());
  }

  /**
   * Removes all landscapes in the repository.
   */
  void clear();


}
