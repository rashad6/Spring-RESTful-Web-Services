package com.raschad.app.ws.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UtilsTest {

	
	@Autowired
	Utils utils;
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGenerateUserId() {
		//fail("Not yet implemented");
		String userId = utils.generateUserId(30);
		String userId2 = utils.generateUserId(30);

		assertNotNull(userId);
		assertNotNull(userId2);

		assertTrue(userId.length()==30);
		assertTrue( !userId.equalsIgnoreCase(userId2) );
	}

	@Test
	void testHasTokenNotExpired() {
		
		String token = utils.generateEmailVerificationToken("564daskjdghasd");
		assertNotNull(token);
		
		boolean hasTokenExpired = Utils.hasTokenExpired(token);
		
		assertFalse(hasTokenExpired);
		
	}
	@Test
	void testHasTokenExpired()
	{
		String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJaR2xZOUJSeW03ZkFEcHVKcGtxV016VDY4UnF6bnAiLCJleHAiOjE2NjM5MjAxNDh9.YYPh3mFddVBMUO8I1PMylK_D17cOSlSN89YTklXBGdgtrT0IJO2H8UJoNvV1dJPz9q9uwk_mAuhHhqovEaRshw";
		boolean hasTokenExpired = Utils.hasTokenExpired(expiredToken);
		
		assertTrue(hasTokenExpired);

	}

}
