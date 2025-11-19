package com.example.backend.dto;

public class UserDto {
	
	 private Long id;
	    private String username;
	    private String email;
	    private String role;
		private String created_At;

		public UserDto() {
		}

		public UserDto(Long id, String username, String email, String role, String created_At) {
			this.id = id;
			this.username = username;
			this.email = email;
			this.role = role;
			this.created_At = created_At;
		}

	    
	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public String getEmail() {
	        return email;
	    }

	    public void setEmail(String email) {
	        this.email = email;
	    }

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getCreated_At() {
			return created_At;
		}

		public void setCreated_At(String created_At) {
			this.created_At = created_At;
		}

}
