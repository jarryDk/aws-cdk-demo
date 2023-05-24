package dk.jarry.todo.boundary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import dk.jarry.todo.entity.ToDo;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@ApplicationScoped
public class ToDoService extends AbstractService {

	@Inject
	DynamoDbClient dynamoDB;

	public ToDo create(ToDo toDo) {
		dynamoDB.putItem(getPutItemRequest(toDo));
		return toDo;
	}

	public Optional<ToDo> read(String uuid) {
		GetItemResponse getItem = dynamoDB.getItem(getGetItemRequest(uuid));
		if (getItem.hasItem()) {
			return Optional.of(ToDo.from(getItem.item()));
		}
		return Optional.empty();
	}

	public Optional<ToDo> update(String uuid, ToDo toDo) {
		UpdateItemResponse updateItem = dynamoDB.updateItem(getUpdateItemRequest(uuid, toDo));
		if (updateItem.hasAttributes()) {
			return Optional.of(ToDo.from(updateItem.attributes()));
		}
		return Optional.empty();
	}

	public void delete(String uuid) {
		dynamoDB.deleteItem(getDeleteItemRequest(uuid));
	}

	public List<ToDo> findAll() {
		return dynamoDB.scanPaginator(getScanRequest()).items().stream() //
				.map(ToDo::from) //
				.collect(Collectors.toList());
	}

	@Provider
	public static class ErrorMapper implements ExceptionMapper<Exception> {

		@Override
		public Response toResponse(Exception exception) {
			int code = 500;
			if (exception instanceof WebApplicationException) {
				code = ((WebApplicationException) exception).getResponse().getStatus();
			}
			return Response.status(code).entity(Json.createObjectBuilder() //
					.add("error", (exception.getMessage() != null ? exception.getMessage() : "")) //
					// .add("stackTrace", stackTrace(exception)) //
					.add("code", code).build()).build();
		}

		String stackTrace(Exception exception) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			return writer.toString();
		}

	}

}
