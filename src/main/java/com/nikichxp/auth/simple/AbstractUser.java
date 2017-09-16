package com.nikichxp.auth.simple;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class AbstractUser {
	
	@Id
	private String id;
}
