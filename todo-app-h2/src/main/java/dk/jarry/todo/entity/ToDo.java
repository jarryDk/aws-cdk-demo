package dk.jarry.todo.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class ToDo {
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
        )
        @Column(name = "uuid", updatable = false, nullable = false)
        public UUID uuid;
        public String subject;
        public String body;
}
