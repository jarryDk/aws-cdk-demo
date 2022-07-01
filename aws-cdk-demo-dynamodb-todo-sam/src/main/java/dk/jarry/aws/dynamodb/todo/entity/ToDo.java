package dk.jarry.aws.dynamodb.todo.entity;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import dk.jarry.aws.dynamodb.todo.boundary.AbstractService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RegisterForReflection
public class ToDo {

	public String uuid;
	public String subject;
	public String body;

	public ToDo() {
		this.uuid = UUID.randomUUID().toString();
	}

	public static ToDo from(Map<String, AttributeValue> item) {
		ToDo toDo = new ToDo();
		if (item != null && !item.isEmpty()) {
			toDo.setUuid(item.get(AbstractService.TODO_UUID_COL).s());
			toDo.setSubject(item.get(AbstractService.TODO_SUBJECT_COL).s());
			toDo.setBody(item.get(AbstractService.TODO_BODY_COL).s());
		}
		return toDo;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, subject, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ToDo other = (ToDo) obj;
		return Objects.equals(body, other.body) && Objects.equals(subject, other.subject)
				&& Objects.equals(uuid, other.uuid);
	}
	
}
