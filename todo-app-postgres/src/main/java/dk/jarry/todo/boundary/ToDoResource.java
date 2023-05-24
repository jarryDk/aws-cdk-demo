package dk.jarry.todo.boundary;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import dk.jarry.todo.entity.ToDo;

@Path("/todos")
public class ToDoResource {

    @Inject
	ToDoService toDoService;

	@POST
	public ToDo create(ToDo toDo) {
		return toDoService.create(toDo);
	}

	@GET
	@Path("{uuid}")
	public ToDo read(@PathParam("uuid") UUID uuid) {
		return toDoService.read(uuid);
	}

	@PUT
	@Path("{uuid}")
	public ToDo update(@PathParam("uuid") UUID uuid, ToDo toDo) {
		return toDoService.update(uuid, toDo);
	}

	@DELETE
	@Path("{uuid}")	
	public void delete(@PathParam("uuid") UUID uuid) {
		toDoService.delete(uuid);
	}

	@GET
	public List<ToDo> list( //
		@DefaultValue("0") @QueryParam("from") Integer from, //
		@DefaultValue("100") @QueryParam("limit") Integer limit) {
		return toDoService.list(from, limit);
	}

}