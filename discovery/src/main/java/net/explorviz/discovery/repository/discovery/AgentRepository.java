package net.explorviz.discovery.repository.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import net.explorviz.discovery.server.services.BroadcastService;
import net.explorviz.shared.discovery.model.Agent;
import net.explorviz.shared.discovery.model.Procezz;

public class AgentRepository {

  // private static final Logger LOGGER = LoggerFactory.getLogger(AgentRepository.class);

  private final AtomicLong idGenerator = new AtomicLong(0);

  private final List<Agent> agents = new ArrayList<>();

  private final BroadcastService broadcastService;

  @Inject
  public AgentRepository(final BroadcastService broadcastService) {
    this.broadcastService = broadcastService;
  }

  public String getUniqueIdString() {
    return String.valueOf(this.idGenerator.incrementAndGet());
  }

  public List<Agent> getAgents() {
    return this.agents;
  }

  public Agent lookupAgent(final Agent agent) {
    synchronized (this.agents) {
      return this.agents.stream().filter(Objects::nonNull).filter(a -> a.equals(agent)).findFirst()
          .orElse(null);
    }
  }

  public Agent registerAgent(final Agent agent) {
    synchronized (this.agents) {
      final Agent possibleOldAgent = this.lookupAgent(agent);

      if (possibleOldAgent == null) {
        agent.setId(this.getUniqueIdString());
      } else {
        // re-registration
        // take old agent ID for new agent, since
        // otherwise Ember Store won't update the view for
        // an old remaining agent
        agent.setName(possibleOldAgent.getName());
        agent.setId(possibleOldAgent.getId());
        this.getAgents().remove(possibleOldAgent);
      }

      agent.setLastDiscoveryTime(System.currentTimeMillis());
      agent.setProcezzes(new ArrayList<Procezz>());
      this.getAgents().add(agent);
    }

    return agent;
  }

  public List<Procezz> insertIdsInProcezzList(final List<Procezz> procezzList) {

    for (final Procezz p : procezzList) {
      p.setId(this.getUniqueIdString());
    }

    return procezzList;

  }

  public Optional<Agent> lookupAgentById(final String id) {
    synchronized (this.agents) {
      for (final Agent agent : this.agents) {
        if (agent.getId().equals(id)) {
          return Optional.of(agent);
        }
      }
    }

    return Optional.empty();
  }

  public void updateAgent(final Agent a) {
    synchronized (this.agents) {
      final Agent potentialAgent = this.lookupAgent(a);

      if (potentialAgent != null) {
        this.agents.remove(potentialAgent);
        this.agents.add(a);
        this.broadcastService.broadcastMessage(this.agents);
      }
    }


  }

}
