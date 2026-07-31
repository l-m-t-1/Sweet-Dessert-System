package com.sweet.dessertsystem;

import com.sweet.dessertsystem.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"app.jwt-secret=test-only-jwt-secret-with-at-least-32-bytes"
})
class DessertSystemApplicationTests {

	@Autowired
	private UserMapper userMapper;

	@Test
	void registersUserMapper() {
		assertThat(userMapper).isNotNull();
	}

}
