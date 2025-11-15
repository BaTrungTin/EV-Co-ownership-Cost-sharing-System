package com.evcoownership.coowner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 15)
    
    // chi chu va so, khong bat dau bang so
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$")
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6,max =20)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
