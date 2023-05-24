package dk.jarry.aws.dynamodb.todo.boundary;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import dk.jarry.aws.dynamodb.todo.entity.ToDo;
import io.smallrye.mutiny.Uni;

@Path("/async-todos")
public class ToDoAsyncResource {

	@Inject
	ToDoAsyncService service;

	@POST
	public Uni<ToDo> create(ToDo toDo) {
		return service.create(toDo);
	}

	@GET
	@Path("{uuid}")
	public Uni<ToDo> read(@PathParam("uuid") String uuid) {
		return service.read(uuid);
	}

	@PUT
	@Path("{uuid}")
	public Uni<ToDo> update(@PathParam("uuid") String uuid, ToDo toDo) {
		return service.update(uuid, toDo);
	}

	@DELETE
	@Path("{uuid}")
	public void delete(@PathParam("uuid") String uuid) {
		service.delete(uuid);
	}

	@GET
	public Uni<List<ToDo>> getAll() {
		return service.findAll();
	}

}