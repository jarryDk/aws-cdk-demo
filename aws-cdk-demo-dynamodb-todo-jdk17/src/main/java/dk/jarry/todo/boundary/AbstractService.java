package dk.jarry.todo.boundary;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dk.jarry.todo.entity.ToDo;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

public abstract class AbstractService {

	@Inject
	@ConfigProperty(defaultValue = "todos", name = "dynamoDbTableName")
	String dynamoDbTableName;

	public final static String TODO_UUID_COL = "uuid";
	public final static String TODO_SUBJECT_COL = "subject";
	public final static String TODO_BODY_COL = "body";

	public String getTableName() {
		return dynamoDbTableName;
	}

	protected ScanRequest getScanRequest() {
		return ScanRequest.builder() //
				.tableName(getTableName()) //
				.attributesToGet( //
						TODO_UUID_COL, //
						TODO_SUBJECT_COL, //
						TODO_BODY_COL //
				).build();
	}

	/**
	 * Create
	 *
	 * @param toDo
	 * @return
	 */
	protected PutItemRequest getPutItemRequest(ToDo toDo) {

		Map<String, AttributeValue> item = new HashMap<>();
		item.put(TODO_UUID_COL, AttributeValue.builder().s(toDo.getUuid()).build());
		item.put(TODO_SUBJECT_COL, AttributeValue.builder().s(toDo.getSubject()).build());
		item.put(TODO_BODY_COL, AttributeValue.builder().s(toDo.getBody()).build());

		return PutItemRequest.builder() //
				.tableName(getTableName()) //
				.item(item) //
				.build();
	}

	/**
	 * Read
	 *
	 * @param uuid
	 * @return
	 */
	protected GetItemRequest getGetItemRequest(String uuid) {

		Map<String, AttributeValue> key = new HashMap<>();
		key.put(TODO_UUID_COL, AttributeValue.builder().s(uuid).build());

		return GetItemRequest.builder() //
				.tableName(getTableName()) //
				.key(key) //
				.attributesToGet( //
						TODO_UUID_COL, //
						TODO_SUBJECT_COL, //
						TODO_BODY_COL //
				).build();
	}

	protected UpdateItemRequest getUpdateItemRequest(String uuid, ToDo toDo) {

		Map<String, AttributeValue> key = new HashMap<>();
		key.put(TODO_UUID_COL, AttributeValue.builder().s(uuid).build());

		Map<String, AttributeValue> items = new HashMap<>();
		items.put(":" + TODO_SUBJECT_COL, AttributeValue.builder().s(toDo.getSubject()).build());
		items.put(":" + TODO_BODY_COL, AttributeValue.builder().s(toDo.getBody()).build());

		String updateExpression = "SET " + //
				TODO_SUBJECT_COL + " = :" + TODO_SUBJECT_COL + ", " + //
				TODO_BODY_COL + " = :" + TODO_BODY_COL;

		return UpdateItemRequest.builder() //
				.tableName(getTableName()) //
				.key(key) //
				.returnValues(ReturnValue.ALL_NEW) //
				.updateExpression(updateExpression) //
				.expressionAttributeValues(items).build();
	}

	protected DeleteItemRequest getDeleteItemRequest(String uuid) {

		Map<String, AttributeValue> key = new HashMap<>();
		key.put(TODO_UUID_COL, AttributeValue.builder().s(uuid).build());

		return DeleteItemRequest.builder() //
				.tableName(getTableName()) //
				.key(key) //
				.build();
	}

}
