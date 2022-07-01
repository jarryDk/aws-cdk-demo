package dk.jarry.todo.boundary;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import dk.jarry.todo.entity.ToDo;


@Path("/todos")
public class ToDoResource {

	@Inject
	@ConfigProperty(defaultValue = "localhost", name = "message")
	private String message;

	@Inject
	ToDoService toDoService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return "Hello RESTEasy from " + message;
	}

	@POST
	@Operation(description = "Create a new todo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(ToDo toDo) {
		return Response.status(201) //
				.entity(toDoService.create(toDo)) //
				.build();
	}

	@GET
	@Path("{uuid}")
	@Operation(description = "Get a specific todo by uuid")
	public ToDo read(@PathParam("uuid") String uuid) {
		return toDoService.read(uuid);
	}

	@PUT
	@Path("{uuid}")
	@Operation(description = "Update an exiting todo")
	public ToDo update(@PathParam("uuid") String uuid, ToDo toDo) {
		return toDoService.update(uuid, toDo);
	}

	@DELETE
	@Path("{uuid}")
	@Operation(description = "Delete a specific todo by uuid")
	public void delete(@PathParam("uuid") String uuid) {
		toDoService.delete(uuid);
	}

	@GET
	@Operation(description = "Get all the todos")
	public List<ToDo> findAll( //
			@DefaultValue("0") @QueryParam("from") Integer from, //
			@DefaultValue("100") @QueryParam("limit") Integer limit) {
		return toDoService.findAll();
	}
}