package com.shubh;

import java.util.List;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

/**
 * The Interface MQAccessGateway is responsible to get or put messages to Queue.
 */
public interface MQAccessGateway {

	/**
	 * Gets the MQ queue manager.
	 *
	 * @param queueManager the queue manager
	 * @param mqProperties the mq properties
	 * @param mqConnManager the mq conn manager
	 * @return the MQ queue manager
	 * @throws Exception the critical exception
	 */
	public MQQueueManager getMQQueueManager(String queueManager) throws MQException;

	/**
	 * Commit queue transaction.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @throws Exception the critical exception
	 */
	public void commitQueueTransaction(MQQueueManager mqQueueManager) throws Exception;

	/**
	 * Rollback queue transaction.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @throws Exception the critical exception
	 */
	public void rollbackQueueTransaction(MQQueueManager mqQueueManager) throws Exception;

	/**
	 * Gets the message.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @return the message
	 * @throws Exception the critical exception
	 */
	public String getMessage(MQQueueManager mqQueueManager, String queueName) throws MQException;

	/**
	 * Put message.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @param message the message
	 * @throws Exception the critical exception
	 */
	public void putMessage(MQQueueManager mqQueueManager, String queueName, String message) throws MQException;

	/**
	 * Gets the messages.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @param batchSize the batch size
	 * @return the messages
	 * @throws Exception the critical exception
	 */
	public List<String> getMessages(MQQueueManager mqQueueManager, String queueName, int batchSize) throws Exception;

	/**
	 * Close MQ queue manager.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @throws Exception the critical exception
	 */
	public void closeMQQueueManager(MQQueueManager mqQueueManager) throws Exception;

	/**
	 * Gets the current queue depth.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @return the current queue depth
	 * @throws Exception the critical exception
	 */
	public int getCurrentQueueDepth(MQQueueManager mqQueueManager, String queueName) throws Exception;

	/**
	 * Put messages.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @param messages the messages
	 * @throws Exception the critical exception
	 */
	public void putMessages(MQQueueManager mqQueueManager, String queueName, List<String> messages)
			throws Exception;
}