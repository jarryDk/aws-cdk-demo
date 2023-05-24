package dk.jarry.todo.boundary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import dk.jarry.todo.entity.ToDo;

@RequestScoped
public class ToDoService {

    @Inject
    EntityManager entityManager;

    public ToDoService() {
    }

    @Transactional
    public ToDo create(ToDo toDo) {
        if (toDo.uuid != null) {
            throw new WebApplicationException( //
                    "ToDo not valid.", //
                    Response.Status.BAD_REQUEST);
        }
        entityManager.persist(toDo);
        entityManager.flush();
        entityManager.refresh(toDo);
        return toDo;
    }

    @Transactional
    public ToDo read(UUID uuid) {
        ToDo toDo = entityManager.find(ToDo.class, uuid);
        if (toDo != null) {
            return toDo;
        } else {
            throw new WebApplicationException( //
                    "ToDo with uuid of " + uuid + " does not exist.", //
                    Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public ToDo update(UUID uuid, ToDo toDo) {
        if (read(uuid) != null) {
            return entityManager.merge(toDo);
        } else {
            throw new WebApplicationException( //
                    "ToDo with uuid of " + uuid + " does not exist.", //
                    Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public void delete(UUID uuid) {
        ToDo toDo = read(uuid);
        if (toDo != null) {
            entityManager.remove(toDo);
        } else {
            throw new WebApplicationException( //
                    "ToDo with uuid of " + uuid + " does not exist.", //
                    Response.Status.NOT_FOUND);
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ToDo> list(Integer start, Integer limit) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ToDo> criteriaQuery = criteriaBuilder.createQuery(ToDo.class);

        Root<ToDo> from = criteriaQuery.from(ToDo.class);

        criteriaQuery.select(from);
        criteriaQuery.orderBy( //
                criteriaBuilder.asc(from.get("subject")), //
                criteriaBuilder.desc(from.get("uuid")));

        Query jpqlQuery = entityManager.createQuery(criteriaQuery);

        jpqlQuery.setFirstResult((start > 0 ? start - 1 : 0));
        jpqlQuery.setMaxResults((limit > 0 ? limit : 100));

        return jpqlQuery.getResultList();

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
