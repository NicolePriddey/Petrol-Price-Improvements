//package PPC.Backend;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.hamcrest.Matchers.containsString;
//
//class BackendTest {
//	
//	
//	@Autowired
//	private Controller controller;
//	
//	@Autowired
//	private MockMvc mockMvc;
//	
//	
//	@Test
//	public void contextLoads() throws Exception {
//		assertThat(controller).isNotNull();
//	}
//	
//	@Test
//	public void mockTestServer() throws Exception {
//		this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("Hello World")));
//	}
//
//}
