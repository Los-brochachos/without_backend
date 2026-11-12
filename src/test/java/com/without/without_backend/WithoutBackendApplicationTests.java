package com.without.without_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.without.without_backend.support.TestDatabaseInitializer;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseInitializer.class)
class WithoutBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
