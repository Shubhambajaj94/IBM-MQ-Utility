package com.shubh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.pcf.CMQC;

/**
 * The Class MQAccessGatewayImpl is responsible to get or put messages to Queue.
 *
 * @author KXT53602
 */
public class MQAccessGatewayImpl implements MQAccessGateway {

	public MQAccessGatewayImpl(String ip, int port, String channel) {
		MQEnvironment.hostname = ip;
		MQEnvironment.port = port;
		MQEnvironment.channel = channel;
	}

	/**
	 * Gets the MQ queue manager.
	 *
	 * @param queueManager the queue manager
	 * @param mqProperties the mq properties
	 * @param mqConnManager the mq conn manager
	 * @return the MQ queue manager
	 * @throws Exception the critical exception
	 */
	@Override
	public MQQueueManager getMQQueueManager(String queueManager) throws MQException {
		MQQueueManager mqQueueManager = null;
		mqQueueManager = new MQQueueManager(queueManager);
		return mqQueueManager;
	}

	/**
	 * Commit queue transaction.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @throws Exception the critical exception
	 */
	@Override
	public void commitQueueTransaction(MQQueueManager mqQueueManager) throws Exception {

		try {
			if (mqQueueManager == null) {
				return;
			}
			mqQueueManager.commit();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		} finally {
			closeMQQueueManager(mqQueueManager);
		}

	}

	/**
	 * Rollback queue transaction.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @throws Exception the critical exception
	 */
	@Override
	public void rollbackQueueTransaction(MQQueueManager mqQueueManager) throws Exception {

		try {
			if (mqQueueManager == null) {
				return;
			}
			mqQueueManager.backout();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		} finally {
			closeMQQueueManager(mqQueueManager);
		}

	}

	/**
	 * Gets the message.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @return the message
	 * @throws Exception the critical exception
	 */
	@Override
	public String getMessage(MQQueueManager mqQueueManager, String queueName) throws MQException {

		MQQueue mqQueue = null;
		String messages = null;
		try {
			// int options = 8226;
			// CMQC.MQOO_INPUT_EXCLUSIVE:- pen the queue to get messages with exclusive
			// access. CMQC.MQOO_FAIL_IF_QUIESCING:- The MQOPEN call fails if the queue
			// manager is in quiescing state. CMQC.MQOO_INQUIRE:- Open the object to query
			// attributes.
			int options = CMQC.MQOO_INPUT_SHARED | CMQC.MQOO_FAIL_IF_QUIESCING | CMQC.MQOO_INQUIRE;
			// Connecting to specified Queue.
			mqQueue = mqQueueManager.accessQueue(queueName, options);
			MQGetMessageOptions localMQGetMessageOptions = new MQGetMessageOptions();
			// CMQC.MQGMO_SYNCPOINT - The request is to operate within the normal
			// unit-of-work protocols.
			//localMQGetMessageOptions.options = CMQC.MQGMO_SYNCPOINT;
			MQMessage mqMessage = new MQMessage();
			mqQueue.get(mqMessage, localMQGetMessageOptions);
			byte[] arrayOfByte = new byte[mqMessage.getTotalMessageLength()];
			mqMessage.readFully(arrayOfByte);
			messages = new String(arrayOfByte);
		} catch (IOException ex) {
			throw new MQException(2, 20000, ex.getMessage());
		} finally {
			closeMQQueue(mqQueue);
		}

		return messages;
	}

	/**
	 * Gets the messages.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @param batchSize the batch size
	 * @return the messages
	 * @throws Exception the critical exception
	 */
	@Override
	public List<String> getMessages(MQQueueManager mqQueueManager, String queueName, int batchSize) throws Exception {
		MQQueue mqQueue = null;
		List<String> inMessages = new ArrayList<String>();

		try {
			//int options = 8226;
			// CMQC.MQOO_INPUT_EXCLUSIVE:- pen the queue to get messages with exclusive
			// access. CMQC.MQOO_FAIL_IF_QUIESCING:- The MQOPEN call fails if the queue
			// manager is in quiescing state. CMQC.MQOO_INQUIRE:- Open the object to query
			// attributes.
			int options = CMQC.MQOO_INPUT_SHARED | CMQC.MQOO_FAIL_IF_QUIESCING | CMQC.MQOO_INQUIRE;
			// Connecting to specified Queue.
			mqQueue = mqQueueManager.accessQueue(queueName, options);

			int currentQueueDepth = mqQueue.getCurrentDepth();
			if (currentQueueDepth == 0) {
				return inMessages;
			}

			MQGetMessageOptions localMQGetMessageOptions = new MQGetMessageOptions();
			// CMQC.MQGMO_SYNCPOINT - The request is to operate within the normal
			// unit-of-work protocols.
			localMQGetMessageOptions.options = CMQC.MQGMO_SYNCPOINT;

			// Validating whether current queue depth is less or equal to batch size to read
			// number of messages from Queue accordingly.
			int messageBatchSize = currentQueueDepth <= batchSize ? currentQueueDepth : batchSize;

			// Reading messages from MQ.
			for (int qDepth = 0; qDepth < messageBatchSize; qDepth++) {
				MQMessage mqMessage = new MQMessage();
				mqQueue.get(mqMessage, localMQGetMessageOptions);

				byte[] arrayOfByte = new byte[mqMessage.getTotalMessageLength()];
				mqMessage.readFully(arrayOfByte);

				// Converting message type byte to String.
				String message = new String(arrayOfByte);
				inMessages.add(message);
			}
		} catch (Exception ex) {
				throw new Exception( ex.getMessage());
		} finally {
			closeMQQueue(mqQueue);
		}

		return inMessages;
	}

	/**
	 * Put message.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @param message the message
	 * @throws Exception the critical exception
	 */
	@Override
	public void putMessage(MQQueueManager mqQueueManager, String queueName, String message) throws MQException {
		MQQueue mqQueue = null;

		try {
			// int options = 24592;
			// Open the queue to put messages. The queue is opened for use with subsequent
			// MQPUT calls.
			int options = CMQC.MQOO_OUTPUT | CMQC.MQOO_FAIL_IF_QUIESCING;
			mqQueue = mqQueueManager.accessQueue(queueName, options);
			MQPutMessageOptions putOptions = new MQPutMessageOptions();
			putOptions.options = CMQC.MQGMO_NONE;
			MQMessage mqMessage = new MQMessage();
			mqMessage.format = "";
			mqMessage.writeString(message);
			mqQueue.put(mqMessage, putOptions);
		} catch (IOException ex) {
			throw new MQException(2, 20000, ex.getMessage());
		} finally {
			closeMQQueue(mqQueue);
		}

	}

	/**
	 * Put messages.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @param messages the messages
	 * @throws Exception the critical exception
	 */
	@Override
	public void putMessages(MQQueueManager mqQueueManager, String queueName, List<String> messages)
			throws Exception {
		MQQueue mqQueue = null;

		try {
			// int options = 24592;
			// Open the queue to put messages. The queue is opened for use with subsequent
			// MQPUT calls.
			int options = CMQC.MQOO_OUTPUT;
			mqQueue = mqQueueManager.accessQueue(queueName, options);
			MQPutMessageOptions putOptions = new MQPutMessageOptions();
			putOptions.options = CMQC.MQGMO_NONE;
			for (String message : messages) {
				MQMessage mqMessage = new MQMessage();
				mqMessage.format = "";
				mqMessage.writeString(message);
				mqQueue.put(mqMessage, putOptions);
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		} finally {
			closeMQQueue(mqQueue);
		}

	}

	/**
	 * Close MQ queue manager.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @throws Exception the critical exception
	 */
	@Override
	public void closeMQQueueManager(MQQueueManager mqQueueManager) throws Exception {

		try {
			if (mqQueueManager == null) {
				return;
			}
			mqQueueManager.disconnect();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}

	}

	/**
	 * Close MQ queue.
	 *
	 * @param mqQueue the mq queue
	 * @throws Exception the critical exception
	 */
	private void closeMQQueue(MQQueue mqQueue) throws MQException {

			if (mqQueue == null) {
				return;
			}
			mqQueue.close();
	}

	/**
	 * Gets the current queue depth.
	 *
	 * @param mqQueueManager the mq queue manager
	 * @param queueName the queue name
	 * @return the current queue depth
	 * @throws Exception the critical exception
	 */
	@Override
	public int getCurrentQueueDepth(MQQueueManager mqQueueManager, String queueName) throws Exception {
		int queueDepth = -1;
		MQQueue mqQueue = null;

		try {
			if (mqQueueManager == null) {
				return queueDepth;
			}
			int options = 0;
			
			options = CMQC.MQOO_INPUT_SHARED | CMQC.MQOO_FAIL_IF_QUIESCING | CMQC.MQOO_INQUIRE;

			// Connecting to specified MQ.
			mqQueue = mqQueueManager.accessQueue(queueName, options);

			queueDepth = mqQueue.getCurrentDepth();
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		} finally {
			closeMQQueue(mqQueue);
		}

		return queueDepth;
	}

}
