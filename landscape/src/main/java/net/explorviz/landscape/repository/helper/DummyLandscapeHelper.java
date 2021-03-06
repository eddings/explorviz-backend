package net.explorviz.landscape.repository.helper;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import net.explorviz.landscape.repository.LandscapeDummyCreator;
import net.explorviz.shared.landscape.model.application.Application;
import net.explorviz.shared.landscape.model.application.ApplicationCommunication;
import net.explorviz.shared.landscape.model.application.Clazz;
import net.explorviz.shared.landscape.model.application.Component;
import net.explorviz.shared.landscape.model.event.EEventType;
import net.explorviz.shared.landscape.model.helper.EProgrammingLanguage;
import net.explorviz.shared.landscape.model.helper.ModelHelper;
import net.explorviz.shared.landscape.model.landscape.Landscape;
import net.explorviz.shared.landscape.model.landscape.Node;
import net.explorviz.shared.landscape.model.landscape.NodeGroup;
import net.explorviz.shared.landscape.model.landscape.System;

/**
 * Helper class providing methods for creating a dummy landscape.
 */
public final class DummyLandscapeHelper {

  private DummyLandscapeHelper() {
    // Utility Class
  }

  /**
   * Returns a random number between minValue and maxValue.
   *
   * @param minValue - Minimum random value
   * @param maxValue - Maximum random value
   * @return random number
   */
  public static int getRandomNum(final int minValue, final int maxValue) {
    if (minValue > 0 && maxValue > 0) {
      return ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
    }
    return 0;
  }

  /**
   * Returns a unique next sequence number.
   *
   * @return Next unique number
   */
  public static int getNextSequenceId() {
    final AtomicInteger seqId = new AtomicInteger();
    return seqId.getAndIncrement();
  }

  /**
   * Creates a new system
   *
   * @param name - name of the system
   * @param parentLandscape - parent landscape
   * @return created system
   */
  public static System createSystem(final String name, final Landscape parentLandscape) {
    final System system = new System();
    system.initializeId();
    system.setName(name);
    system.setParent(parentLandscape);

    // create new system event
    LandscapeHelper.createNewEvent(parentLandscape, EEventType.NEWSYSTEM,
        "New system '" + system.getName() + "' detected");

    return system;
  }

  /**
   * Creates a new nodeGroup
   *
   * @param name - name of the nodeGroup
   * @param system - parent system
   * @return created nodeGroup
   */
  public static NodeGroup createNodeGroup(final String name, final System system) {
    final NodeGroup nodeGroup = new NodeGroup();
    nodeGroup.initializeId();
    nodeGroup.setName(name);
    nodeGroup.setParent(system);
    return nodeGroup;
  }

  /**
   * Creates a new node
   *
   * @param ipAddress - ipAddress of the node
   * @param parentNodeGroup - parent nodeGroup
   * @return created node
   */
  public static Node createNode(final String ipAddress, final NodeGroup parentNodeGroup,
      final Landscape landscape) {
    final Node node = new Node();
    node.initializeId();
    node.setIpAddress(ipAddress);
    node.setParent(parentNodeGroup);

    // set random usage
    node.setCpuUtilization((double) getRandomNum(10, 100) / 100);
    node.setFreeRAM((long) getRandomNum(1, 4) * LandscapeDummyCreator.formatFactor);
    node.setUsedRAM((long) getRandomNum(1, 4) * LandscapeDummyCreator.formatFactor);

    // create a new node event
    LandscapeHelper.createNewEvent(landscape, EEventType.NEWNODE, "New node '" + node.getIpAddress()
        + "' in system '" + parentNodeGroup.getParent().getName() + "' detected");

    return node;
  }

  /**
   * Creates a new application
   *
   * @param name - name of the application
   * @param parentNode - name of the parent node
   * @param landscape - name of the landscape
   * @return created application
   */
  public static Application createApplication(final String name, final Node parentNode,
      final Landscape landscape) {
    final Application application = new Application();
    application.initializeId();

    LandscapeDummyCreator.applicationId = LandscapeDummyCreator.applicationId + 1;
    application.setParent(parentNode);

    application.setLastUsage(java.lang.System.currentTimeMillis());
    application.setProgrammingLanguage(EProgrammingLanguage.JAVA);

    if (name == "Eprints") {
      application.setProgrammingLanguage(EProgrammingLanguage.PERL);
    }

    application.setName(name);
    parentNode.getApplications().add(application);

    // create a new application event
    LandscapeHelper.createNewEvent(landscape, EEventType.NEWNODE, "New node '" + application.getName()
        + "' in system '" + parentNode.getName() + "' detected");

    return application;
  }

  /**
   * Create communication between applications
   *
   * @param source - sourceApplication
   * @param target - targetApplication
   * @param landscape - parent landscape
   * @param requests - number of requests
   * @return created ApplicationCommunication
   */
  public static ApplicationCommunication createApplicationCommunication(final Application source,
      final Application target, final Landscape landscape, final int requests) {
    final ApplicationCommunication communication = new ApplicationCommunication();
    communication.initializeId();
    communication.setSourceApplication(source);
    communication.setTargetApplication(target);
    communication.setRequests(requests);

    final float averageResponseTime = 0L + getRandomNum(10, 1000);
    communication.setAverageResponseTime(averageResponseTime);

    source.getApplicationCommunications().add(communication);
    landscape.getTotalApplicationCommunications().add(communication);

    return communication;
  }

  /**
   * Creates a component
   *
   * @param name - name of the component
   * @param parent - parent component
   * @param app - parent application
   * @return created component
   */
  public static Component createComponent(final String name, final Component parent,
      final Application app) {
    final Component component = new Component();
    component.initializeId();
    component.setName(name);
    component.setParentComponent(parent);
    component.setBelongingApplication(app);
    if (parent == null) {
      component.setFullQualifiedName(name);
    } else {
      component.setFullQualifiedName(parent.getFullQualifiedName() + "." + name);
      parent.getChildren().add(component);
    }
    return component;
  }

  /**
   * Creates a clazz
   *
   * @param name - name of the clazz
   * @param component - parent component
   * @param instanceCount - number of instances
   * @return created clazz
   */
  public static Clazz createClazz(final String name, final Component component,
      final int instanceCount) {
    final Clazz clazz = new Clazz();
    clazz.initializeId();
    clazz.setName(name);
    clazz.setFullQualifiedName(component.getFullQualifiedName() + "." + name);
    clazz.setInstanceCount(instanceCount);
    clazz.setParent(component);
    component.getClazzes().add(clazz);

    return clazz;
  }

  /**
   * Creating a communication between two clazzes within the dummy landscape
   *
   * @param traceId - id of the trace
   * @param requests - number of requests
   * @param sourceClazz - sourceClazz
   * @param targetClazz - targetClazz
   * @param application - parent application
   */
  public static void createClazzCommunication(final String traceId, final int tracePosition,
      final int requests, final Clazz sourceClazz, final Clazz targetClazz,
      final Application application) {

    final float averageResponseTime = 0L + getRandomNum(10, 1000);
    final float overallTraceDuration = 0L + getRandomNum(1000, 10000);
    final String operationName = "getMethod" + getRandomNum(1, 50) + "()";

    ModelHelper.addClazzCommunication(sourceClazz, targetClazz, application, requests,
        averageResponseTime, overallTraceDuration, traceId, tracePosition, operationName);
  }

}
