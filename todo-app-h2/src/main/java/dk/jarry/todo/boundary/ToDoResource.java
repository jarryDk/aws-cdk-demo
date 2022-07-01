package dk.jarry.todo.boundary;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

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