package dk.jarry.aws.dynamodb.todo.boundary;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import dk.jarry.aws.dynamodb.todo.entity.ToDo;
import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@ApplicationScoped
public class ToDoAsyncService extends AbstractService {

	@Inject
	DynamoDbAsyncClient dynamoDB;

	public Uni<ToDo> create(ToDo toDo) {
		return Uni.createFrom().completionStage( //
				() -> dynamoDB.putItem(getPutItemRequest(toDo))) //
				.onItem() //
				.transform(resp -> ToDo.from(resp.attributes()));
	}

	public Uni<ToDo> read(String uuid) {
		return Uni.createFrom().completionStage( //
				() -> dynamoDB.getItem(getGetItemRequest(uuid))) //
				.onItem() //
				.transform(resp -> ToDo.from(resp.item()));
	}

	public Uni<ToDo> update(String uuid, ToDo toDo) {
		return Uni.createFrom().completionStage( //
				() -> dynamoDB.updateItem(getUpdateItemRequest(uuid, toDo))) //
				.onItem() //
				.transform(resp -> ToDo.from(resp.attributes()));
	}

	public void delete(String uuid) {
		Uni.createFrom().completionStage( //
				() -> dynamoDB.deleteItem(getDeleteItemRequest(uuid))).ifNoItem(); //
	}

	public Uni<List<ToDo>> findAll() {
		return Uni.createFrom().completionStage( //
				() -> dynamoDB.scan(getScanRequest())) //
				.onItem().transform(res -> res.items().stream().map(ToDo::from).collect(Collectors.toList()));
	}

}
