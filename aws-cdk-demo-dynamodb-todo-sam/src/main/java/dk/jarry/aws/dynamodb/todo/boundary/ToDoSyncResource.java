package dk.jarry.aws.dynamodb.todo.boundary;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import dk.jarry.aws.dynamodb.todo.entity.ToDo;

@Path("/todos")
public class ToDoSyncResource {

	@Inject
	ToDoSyncService service;

	@POST
	public ToDo create(ToDo toDo) {
		return service.create(toDo);
	}

	@GET
	@Path("{uuid}")
	public ToDo read(@PathParam("uuid") String uuid) {
		return service.read(uuid);
	}

	@PUT
	@Path("{uuid}")
	public ToDo update(@PathParam("uuid") String uuid, ToDo toDo) {
		return service.update(uuid, toDo);
	}

	@DELETE
	@Path("{uuid}")
	public void delete(@PathParam("uuid") String uuid) {
		service.delete(uuid);
	}

	@GET
	public List<ToDo> getAll() {
		return service.findAll();
	}

}