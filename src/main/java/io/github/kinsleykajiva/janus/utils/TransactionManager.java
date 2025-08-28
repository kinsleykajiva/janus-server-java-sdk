package io.github.kinsleykajiva.janus.utils;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TransactionManager {
	private final ConcurrentHashMap<String, CompletableFuture<JSONObject>> transactions = new ConcurrentHashMap<>();
	
	/**
	 * This is where createTransaction is defined.
	 * It generates a unique ID for a request.
	 */
	public String createTransaction() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * This is where registerTransaction is defined.
	 * It creates a placeholder (a CompletableFuture) for a response we expect from Janus.
	 * @return A CompletableFuture that will eventually hold the JSONObject response.
	 */
	public CompletableFuture<JSONObject> registerTransaction(String transactionId) {
		CompletableFuture<JSONObject> future = new CompletableFuture<>();
		transactions.put(transactionId, future);
		return future;
	}
	
	/**
	 * Completes a transaction when a response with a matching transaction ID is received.
	 */
	public void completeTransaction(String transactionId, JSONObject response) {
		CompletableFuture<JSONObject> future = transactions.remove(transactionId);
		if (future != null) {
			if ("error".equals(response.optString("janus"))) {
				future.completeExceptionally(new JanusException(response.getJSONObject("error").optString("reason")));
			} else {
				future.complete(response);
			}
		}
	}
	
	/**
	 * A helper for implementing blocking calls.
	 */
	public JSONObject waitForResponse(String transactionId, CompletableFuture<JSONObject> future, long timeoutMillis)
			throws TimeoutException {
		try {
			return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			transactions.remove(transactionId);
			throw e;
		} catch (Exception e) {
			throw new JanusException("Request failed while waiting for response", e);
		}
	}
}