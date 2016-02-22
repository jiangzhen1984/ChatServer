package co.weeby.service;

import java.nio.channels.DatagramChannel;

import junit.framework.TestCase;
import co.weeby.event.FallbackMessage;
import co.weeby.event.InternalUserRoomMessage;
import co.weeby.event.InternalUserSendMessage;
import co.weeby.event.InternalUserStateMessage;
import co.weeby.event.MessageEvent;
import co.weeby.event.ServerDataAvailableMessage;
import co.weeby.event.UserDataAvailableMessage;
import co.weeby.event.UserRoomMessage;
import co.weeby.event.UserSendMessage;

public class SyncServiceTestCase extends TestCase {
	
	private MessageParserService service;
	private MockServerTerminal terminal;

	protected void setUp() throws Exception {
		service = new MessageParserService();
		terminal = new MockServerTerminal((DatagramChannel)null);
	}

	public void testServerNodeDataAviParser() {
		ServerDataAvailableMessage m1 = new ServerDataAvailableMessage(terminal);
		MessageEvent nextM = null;
		
		//test incorrect internal message
		terminal.setMockMsg("@/wef") ;
		nextM = service.service(m1);
		assertTrue(nextM == null);
		
		terminal.setMockMsg("@") ;
		nextM = service.service(m1);
		assertTrue(nextM == null);
		
		
		terminal.setMockMsg("@/online ") ;
		nextM = service.service(m1);
		assertTrue(nextM == null);
		
		terminal.setMockMsg("@/online aaa") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof InternalUserStateMessage);
		assertEquals("aaa", ((InternalUserStateMessage)nextM).getUserName());
		assertEquals(true, ((InternalUserStateMessage)nextM).isOnline());
		

		terminal.setMockMsg("@/online aaa bbb dd") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof InternalUserStateMessage);
		assertEquals("aaa", ((InternalUserStateMessage)nextM).getUserName());
		assertEquals(true, ((InternalUserStateMessage)nextM).isOnline());
		
		
		terminal.setMockMsg("@/offline aaa") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof InternalUserStateMessage);
		assertEquals(false, ((InternalUserStateMessage)nextM).isOnline());
		
		
		terminal.setMockMsg("@/join aaa roomName") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof InternalUserRoomMessage);
		assertEquals(InternalUserRoomMessage.Action.ENTER, ((InternalUserRoomMessage)nextM).getAc());
		assertEquals("roomName", ((InternalUserRoomMessage)nextM).getRoom());
		assertEquals("aaa", ((InternalUserRoomMessage)nextM).getUserName());
		
		terminal.setMockMsg("@/join aaa ") ;
		nextM = service.service(m1);
		assertTrue(nextM == null);
		
		
		terminal.setMockMsg("@/leave aaa roomName") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof InternalUserRoomMessage);
		assertEquals(InternalUserRoomMessage.Action.LEAVE, ((InternalUserRoomMessage)nextM).getAc());
		assertEquals("roomName", ((InternalUserRoomMessage)nextM).getRoom());
		assertEquals("aaa", ((InternalUserRoomMessage)nextM).getUserName());
		
		
		terminal.setMockMsg("@/send aaa this is message from www") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof InternalUserSendMessage);
		assertEquals("aaa", ((InternalUserSendMessage)nextM).getUserName());
		assertEquals("this is message from www", ((InternalUserSendMessage)nextM).getMsg());
	}
	
	
	
	public void testUserDataAviParser() {
		UserDataAvailableMessage m1 = new UserDataAvailableMessage(terminal);
		MessageEvent nextM = null;
		
		terminal.setMockMsg("wwww  sdsd sde wwe we") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof UserSendMessage);
		assertEquals("wwww  sdsd sde wwe we", ((UserSendMessage)nextM).getMsg());
		
		
		terminal.setMockMsg("/join ") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof FallbackMessage);
		assertEquals("No room names \n", ((FallbackMessage)nextM).getMsg());
		
		terminal.setMockMsg("/joinaaaa") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof FallbackMessage);
		assertEquals("Incorrect format  shoule be /join roomname \n", ((FallbackMessage)nextM).getMsg());
		
		
		terminal.setMockMsg("/join s w g ") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof UserRoomMessage);
		assertEquals(UserRoomMessage.Action.ENTER, ((UserRoomMessage)nextM).getAc());
		assertEquals("s w g", ((UserRoomMessage)nextM).getRoom());
		
		
		
		terminal.setMockMsg("/leave") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof UserRoomMessage);
		assertEquals(UserRoomMessage.Action.LEAVE, ((UserRoomMessage)nextM).getAc());
		
		
		terminal.setMockMsg("/rooms") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof UserRoomMessage);
		assertEquals(UserRoomMessage.Action.LIST, ((UserRoomMessage)nextM).getAc());
		
		terminal.setMockMsg("/roomse") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof FallbackMessage);
		assertEquals("Unknown command : [/join  /leave /quit /rooms] \n", ((FallbackMessage)nextM).getMsg());
	
	

		
		terminal.setMockMsg("/d") ;
		nextM = service.service(m1);
		assertTrue(nextM instanceof FallbackMessage);
		assertEquals("Unknown command : [/join  /leave /quit /rooms] \n", ((FallbackMessage)nextM).getMsg());
	}
	

}

