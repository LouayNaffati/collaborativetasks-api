package com.example.backend.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "img_url")
	private String imgUrl;

	@Column(nullable = false)
	private String name;

	@Column(length = 2000)
	private String description;

	@ManyToMany
	@JoinTable(
		name = "project_collaborators",
		joinColumns = @JoinColumn(name = "project_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private Set<User> collaborators = new HashSet<>();

	public Project() {
	}

	public Project(String imgUrl, String name, String description) {
		this.imgUrl = imgUrl;
		this.name = name;
		this.description = description;
	}

	// Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<User> getCollaborators() {
		return collaborators;
	}

	public void setCollaborators(Set<User> collaborators) {
		this.collaborators = collaborators;
	}

	public void addCollaborator(User user) {
		this.collaborators.add(user);
	}

	public void removeCollaborator(User user) {
		this.collaborators.remove(user);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Project project = (Project) o;
		return Objects.equals(id, project.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
